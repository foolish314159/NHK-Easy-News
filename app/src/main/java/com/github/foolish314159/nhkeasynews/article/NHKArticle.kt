package com.github.foolish314159.nhkeasynews.article

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import com.github.foolish314159.nhkeasynews.util.HTTPRequestHelper
import com.github.foolish314159.nhkeasynews.util.URLHelper
import com.orm.SugarRecord
import org.jsoup.Jsoup
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.*

/**
 * Base class for articles, deals with loading the text either locally or from web
 */
class NHKArticle(val articleId: String, val title: String, val date: Date) : SugarRecord(), Parcelable, Comparable<NHKArticle> {

    constructor() : this("", "", Date()) {
        // empty for SugarRecord
    }

    fun imageUri(activity: Activity): String {
        val folder = activity.filesDir.absolutePath
        return "file://$folder/$articleId.jpg"
    }

    fun soundUri(activity: Activity): String {
        val folder = activity.filesDir.absolutePath
        return "file://$folder/$articleId.mp3"
    }

    fun hasLocalAudio(activity: Activity): Boolean {
        val folder = activity.filesDir.absolutePath
        return File("$folder/$articleId.mp3").exists()
    }

    fun saveImage(activity: Activity, bitmap: Bitmap) {
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, activity.openFileOutput("$articleId.jpg", Context.MODE_PRIVATE))
        } catch (fileNotFound: FileNotFoundException) {
            // ignore
        }
    }


    fun loadArticleText(activity: Activity, handler: (String) -> Unit) {
        activity.filesDir.listFiles().forEach {
            if (it.name.contains("$articleId.html")) {
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
            reader = InputStreamReader(file.inputStream(), Charsets.UTF_8)
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
        val articleURL = URLHelper.articleURL(articleId)
        HTTPRequestHelper.requestTextFromURL(activity, articleURL) {
            it?.let { html ->
                val thread = Thread(Runnable {
                    val text = parseArticleFromHtml(html)
                    val styledText = addStylesheet(text)

                    saveArticleToInternalStorage(activity, styledText)

                    activity.runOnUiThread { handler(styledText) }
                })
                thread.start()
            }
        }
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
                linkCopy.select("rt").forEach {
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

    // TODO: move to .css file
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

    // Comparable

    override fun compareTo(other: NHKArticle): Int {
        // Sort articles by date
        return other.date.compareTo(this.date)
    }

    // Parcelable

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            Date(parcel.readLong()))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(articleId)
        parcel.writeString(title)
        parcel.writeLong(date.time)
    }

    override fun describeContents(): Int {
        return 0
    }


    companion object CREATOR : Parcelable.Creator<NHKArticle> {
        override fun createFromParcel(parcel: Parcel): NHKArticle {
            return NHKArticle(parcel)
        }

        override fun newArray(size: Int): Array<NHKArticle?> {
            return arrayOfNulls(size)
        }
    }

}