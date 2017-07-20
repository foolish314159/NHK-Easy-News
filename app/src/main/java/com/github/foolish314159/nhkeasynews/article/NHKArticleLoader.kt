package com.github.foolish314159.nhkeasynews.article

import android.app.Activity
import android.graphics.BitmapFactory
import com.github.foolish314159.nhkeasynews.util.HTTPRequestHelper
import com.github.foolish314159.nhkeasynews.util.URLHelper
import com.github.foolish314159.nhkeasynews.util.contains
import org.json.JSONArray
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat

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

    /**
     * Load articles from Web and notify handler as soon as an article is read to be displayed
     */
    fun articlesFromWeb(alreadyLoaded: List<NHKArticle>, handler: (NHKArticle) -> Unit) {
        val url = URLHelper.newsListURL()
        HTTPRequestHelper.requestTextFromURL(activity, url, HTTPRequestHelper.CONTENT_TYPE_JSON_UTF8) {
            it?.let { json ->
                val thread = Thread(Runnable {
                    parseJsonArticleList(alreadyLoaded, json) { article ->
                        activity.runOnUiThread { handler(article) }
                    }
                })
                thread.start()
            }
        }
    }

    private fun parseJsonArticleList(alreadyLoaded: List<NHKArticle>, json: String, handler: (NHKArticle) -> Unit) {
        val root = JSONArray(json).getJSONObject(0)
        val days = root.keys()
        for (day in days) {
            val articlesInDay = root.getJSONArray(day)
            val articleRange = 0..(articlesInDay.length() - 1)
            for (i in articleRange) {
                val articleObj = articlesInDay.getJSONObject(i)
                val articleId = articleObj.getString(JSON_KEY_ARTICLE_ID)
                if (alreadyLoaded.contains { it.articleId == articleId }) {
                    continue
                }

                val articleDate = DateFormatter.parse(articleObj.getString(JSON_KEY_ARTICLE_TIME))
                val articleTitle = articleObj.getString(JSON_KEY_ARTICLE_TITLE)
                val article = NHKArticle(articleId, articleTitle, articleDate)

                var input: InputStream? = null
                try {
                    val imageUrl = URLHelper.articleImageURL(articleId)
                    input = URL(imageUrl).content as InputStream
                    val bitmap = BitmapFactory.decodeStream(input)
                    article.saveImage(activity, bitmap)
                } catch (e: Exception) {
                    try {
                        val imageUrl = articleObj.getString(JSON_KEY_ARTICLE_IMAGE)
                        input = URL(imageUrl).content as InputStream
                        val bitmap = BitmapFactory.decodeStream(input)
                        article.saveImage(activity, bitmap)
                    } catch (e: Exception) {
                        // TODO: try another way to load image (apparently there are 3 different kind of URLs and image can have)
                    }
                } finally {
                    article.save()
                    handler(article)
                    input?.close()
                }
            }
        }
    }

}