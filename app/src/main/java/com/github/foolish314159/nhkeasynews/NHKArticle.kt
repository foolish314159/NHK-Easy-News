package com.github.foolish314159.nhkeasynews

import android.app.Activity
import android.content.Context
import org.jsoup.Jsoup
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by Tom on 09.07.2017.
 */
class NHKArticle(id: String, hasVideo: Boolean) {

    val articleId = id
    val hasVideo = hasVideo

    fun loadArticleText(activity: Activity, handler: (String?) -> Unit) {
        activity.filesDir.listFiles().forEach {
            if (it.name.contains(articleId)) {
                // load from local file, if article has been opened
                loadArticleFromInternalStorage(activity, it, handler)
                return
            }
        }

        loadArticleFromWeb(activity, handler)
    }

    private fun loadArticleFromInternalStorage(activity: Activity, file: File, handler: (String?) -> Unit) {
        var reader: InputStreamReader? = null
        try {
            reader = InputStreamReader(file.inputStream())
            val text = reader?.readText()
            handler(text)
        } catch (e: Exception) {
            // if loading from local file fails, try to read from web instead
            loadArticleFromWeb(activity, handler)
        } finally {
            reader?.close()
        }
    }

    private fun loadArticleFromWeb(activity: Activity, handler: (String?) -> Unit) {
        val thread = Thread(Runnable {
            val articleURL = URLHelper.articleURL(articleId)
            var connection: HttpURLConnection? = null
            var reader: InputStreamReader? = null
            try {
                val url = URL(articleURL)
                connection = url.openConnection() as HttpURLConnection
                connection?.setRequestProperty("Content-Type", "text/html; charset=utf-8")
                val input = BufferedInputStream(connection?.inputStream)
                reader = InputStreamReader(input)
                val html = reader.readText()
                var text = parseArticleFromHtml(html)
                saveArticleToInternalStorage(activity, text)
                activity.runOnUiThread { handler(text) }
            } catch (e: Exception) {
                activity.runOnUiThread { handler(null) }
            } finally {
                reader?.close()
                connection?.disconnect()
            }
        })
        thread.start()
    }

    private fun parseArticleFromHtml(html: String): String {
        val doc = Jsoup.parse(html)
        var links = doc.select(".dicWin")
        links.forEach { link ->
            link.select("rt").forEach() {
                it.remove()
            }
            val vocabulary = link.text()
            link.attr("href", "http://jisho.org/search/$vocabulary")
        }
        return doc.select("#newsarticle").first().html()
    }

    private fun saveArticleToInternalStorage(activity: Activity, html: String) {
        var output : OutputStreamWriter? = null
        try {
            output = activity.openFileOutput("$articleId.html", Context.MODE_PRIVATE).writer(Charsets.UTF_8)
            output.write(html)
            output.flush()
        } catch (e: Exception) {
            // ignore, next time will be loaded from web again
        } finally {
            output?.close()
        }
    }

}