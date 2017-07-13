package com.github.foolish314159.nhkeasynews.util

import android.app.Activity
import java.io.BufferedInputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HTTPRequestHelper {

    companion object {
        const val CONTENT_TYPE_HTML_UTF8 = "text/html; charset=utf-8"
        const val CONTENT_TYPE_JSON_UTF8 = "text/json; charset=utf-8"

        /** Read content from GET request to URL. Request is executed on background thread.
         * @param handler called on UI thread when result is available
         * **/
        fun requestTextFromURL(activity: Activity, urlString: String, contentType: String = CONTENT_TYPE_HTML_UTF8, handler: (String) -> Unit) {
            val thread = Thread(Runnable {
                var connection: HttpURLConnection? = null
                var reader: InputStreamReader? = null
                try {
                    val url = URL(urlString)
                    connection = url.openConnection() as HttpURLConnection
                    connection.setRequestProperty("Content-Type", contentType)
                    reader = InputStreamReader(BufferedInputStream(connection.inputStream))

                    val html = reader.readText()
                    activity.runOnUiThread { handler(html) }
                } catch (e: Exception) {
                    activity.runOnUiThread { handler("Could not load article") }
                } finally {
                    reader?.close()
                    connection?.disconnect()
                }
            })
            thread.start()
        }
    }

}