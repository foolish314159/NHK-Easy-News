package com.github.foolish314159.nhkeasynews

import android.app.Activity
import android.content.Context
import org.jsoup.Jsoup
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

/**
 * Base class for articles, deals with loading the text either locally or from web
 */
class NHKArticle(id: String, hasVideo: Boolean) {

    val articleId = id
    val hasVideo = hasVideo

    fun loadArticleText(activity: Activity, handler: (String) -> Unit) {
        activity.filesDir.listFiles().forEach {
            if (it.name.contains(articleId)) {
                // load from local file, if article has been opened
                loadArticleFromInternalStorage(activity, it, handler)
                return
            }
        }

        loadArticleFromWeb(activity, handler)
    }

    private fun loadArticleFromInternalStorage(activity: Activity, file: File, handler: (String) -> Unit) {
        var reader: InputStreamReader? = null
        try {
            reader = InputStreamReader(file.inputStream())
            val text = reader.readText()
            handler(text)
        } catch (e: Exception) {
            // if loading from local file fails, try to read from web instead
            loadArticleFromWeb(activity, handler)
        } finally {
            reader?.close()
        }
    }

    private fun loadArticleFromWeb(activity: Activity, handler: (String) -> Unit) {
        val thread = Thread(Runnable {
            val articleURL = URLHelper.articleURL(articleId)
            var connection: HttpURLConnection? = null
            var reader: InputStreamReader? = null
            try {
                val url = URL(articleURL)
                connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("Content-Type", "text/html; charset=utf-8")
                reader = InputStreamReader(BufferedInputStream(connection.inputStream))

                val html = reader.readText()
                val text = parseArticleFromHtml(html)
                val styledText = addStylesheet(text)

                // TODO: disabled while testing parser
                //saveArticleToInternalStorage(activity, styledText)
                activity.runOnUiThread { handler(styledText) }
            } catch (e: Exception) {
                activity.runOnUiThread { handler("Could not load article") }
            } finally {
                reader?.close()
                connection?.disconnect()
            }
        })
        thread.start()
    }

    /**
     * Read only the div containing the article and change javascript links to jisho.org links
     */
    private fun parseArticleFromHtml(html: String): String {
        try {
            val doc = Jsoup.parse(html)

            val links = doc.select("a.dicWin")
            links.forEach { link ->
                // remove <rt> tags to get the actual vocabulary without furigana (use a copy so we don't lose furigana on the actual text)
                val linkCopy = link.clone()
                linkCopy.select("rt").forEach() {
                    it.remove()
                }
                val vocabulary = linkCopy.text()

                // replace href with the fake URL including vocabulary, will be used by Dictionary
                link.attr("href", "http://translate.this/$vocabulary")
            }

            return doc.select("#newsarticle").first().html()
        } catch (e: Exception) {
            return "Could not load article"
        }
    }

    private fun addStylesheet(html: String): String {
        val style = "<style>" +
                ".colorL { color : #0041ff; }" +
                ".colorC { color : #08a2f1; }" +
                ".colorN { color : #b817af; }" +
                "a.dicWin { color : black; }" +
                "p { font-size: 125%; }" +
                "</style>"
        return "$style$html"
    }

    private fun saveArticleToInternalStorage(activity: Activity, html: String) {
        var output: OutputStreamWriter? = null
        try {
            output = activity.openFileOutput("$articleId.html", Context.MODE_PRIVATE).writer(Charsets.UTF_8)
            output.write(html)
            output.flush()
        } catch (e: Exception) {
            // if we couldn't save file, ignore and next time load from web again
        } finally {
            output?.close()
        }
    }

}