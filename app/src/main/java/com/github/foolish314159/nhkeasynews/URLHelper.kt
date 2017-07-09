package com.github.foolish314159.nhkeasynews

/**
 * Created by Tom on 09.07.2017.
 */
class URLHelper {

    companion object {
        private const val BASE_URL = "http://www3.nhk.or.jp/news/easy/"
        private const val NEWS_LIST_SUFFIX = "news-list.json"

        fun newsListURL(): String {
            return "$BASE_URL$NEWS_LIST_SUFFIX"
        }

        fun articleURL(articleId: String): String {
            return "$BASE_URL$articleId/$articleId.html"
        }

        fun audioURL(articleId: String): String {
            return "$BASE_URL$articleId/$articleId.mp3"
        }
    }

}
