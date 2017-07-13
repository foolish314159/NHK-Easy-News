package com.github.foolish314159.nhkeasynews.article

import android.app.Activity
import android.graphics.drawable.Drawable
import com.github.foolish314159.nhkeasynews.util.HTTPRequestHelper
import com.github.foolish314159.nhkeasynews.util.URLHelper
import org.json.JSONArray
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

/**
 * Loads NHK articles both from internal storage and web
 */
class NHKArticleLoader(val activity: Activity) {

    private val JSON_KEY_ARTICLE_TIME = "news_publication_time"
    private val JSON_KEY_ARTICLE_TITLE = "title_with_ruby"
    private val JSON_KEY_ARTICLE_ID = "news_id"
    private val JSON_KEY_ARTICLE_IMAGE = "news_web_image_uri"


    private val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
    private val DateFormatter = SimpleDateFormat(DATE_FORMAT)

    fun articlesFromWeb(handler: (List<NHKArticle>) -> Unit) {
        val url = URLHelper.newsListURL()
        HTTPRequestHelper.requestTextFromURL(activity, url, HTTPRequestHelper.CONTENT_TYPE_JSON_UTF8) { json ->
            val thread = Thread(Runnable {
                val articles = parseJsonArticleList(json)
                activity.runOnUiThread { handler(articles) }
            })
            thread.start()
        }
    }

    private fun parseJsonArticleList(json: String): List<NHKArticle> {
        val articles = ArrayList<NHKArticle>()

        val root = JSONArray(json).getJSONObject(0)
        val days = root.keys()
        for (day in days) {
            val articlesInDay = root.getJSONArray(day)
            val articleRange = 0..(articlesInDay.length() - 1)
            for (i in articleRange) {
                val articleObj = articlesInDay.getJSONObject(i)
                val articleDate = DateFormatter.parse(articleObj.getString(JSON_KEY_ARTICLE_TIME))
                val articleTitle = articleObj.getString(JSON_KEY_ARTICLE_TITLE)
                val articleId = articleObj.getString(JSON_KEY_ARTICLE_ID)
                val article = NHKArticle(articleId, articleTitle, articleDate)

                var input: InputStream? = null
                try {
                    val imageUrl = URLHelper.articleImageURL(articleId)

                    // TODO: Cache images locally
                    // TODO: Notify UI with single articles instead of waiting till everything finished
                    input = URL(imageUrl).content as InputStream
                    val drawable = Drawable.createFromStream(input, "src")
                    article.image = drawable
                } catch (e: Exception) {
                    try {
                        val imageUrl = articleObj.getString(JSON_KEY_ARTICLE_IMAGE)
                        input = URL(imageUrl).content as InputStream
                        val drawable = Drawable.createFromStream(input, "src")
                        article.image = drawable
                    } catch (e: Exception) {
                        System.err.println(e.localizedMessage)
                    }
                } finally {
                    articles.add(article)
                    input?.close()
                }
            }
        }

        articles.sort()
        return articles
    }

}