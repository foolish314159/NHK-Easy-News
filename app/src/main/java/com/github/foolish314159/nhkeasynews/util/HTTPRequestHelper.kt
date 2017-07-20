package com.github.foolish314159.nhkeasynews.util

import android.app.Activity
import android.content.Context
import com.facebook.common.internal.ByteStreams
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class HTTPRequestHelper {

    companion object {
        const val CONTENT_TYPE_HTML_UTF8 = "text/html; charset=utf-8"
        const val CONTENT_TYPE_JSON_UTF8 = "text/json; charset=utf-8"
        const val CONTENT_TYPE_AUDIO_MPEG = "audio/mpeg"

        /** Read content from GET request to URL. Request is executed on background thread.
         * @param handler called on UI thread when result is available
         * **/
        fun requestTextFromURL(activity: Activity, urlString: String, contentType: String = CONTENT_TYPE_HTML_UTF8, handler: (String?) -> Unit) {
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
                    activity.runOnUiThread { handler(null) }
                } finally {
                    reader?.close()
                    connection?.disconnect()
                }
            })
            thread.start()
        }

        fun downloadUrlToFileSync(activity: Activity, urlString: String, fileName: String): Boolean {
            var success : Boolean
            var connection: HttpURLConnection? = null
            var reader: BufferedInputStream? = null
            var writer: BufferedOutputStream? = null
            try {
                val url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("Content-Type", CONTENT_TYPE_AUDIO_MPEG)
                reader = BufferedInputStream(connection.inputStream)

                val out = activity.openFileOutput(fileName, Context.MODE_PRIVATE)
                writer = BufferedOutputStream(out)
                val buffer = ByteArray(8192)

                while (true) {
                    val read = reader.read(buffer, 0, 8192)
                    if (read != -1) {
                        writer.write(buffer, 0, read)
                        writer.flush()
                    } else {
                        break
                    }
                }
                success = true
            } catch (e: Exception) {
                success = false
            } finally {
                writer?.close()
                reader?.close()
                connection?.disconnect()
            }

            return success
        }

        fun downloadUrlToFileAsync(activity: Activity, urlString: String, fileName: String, handler: (success: Boolean) -> Unit = {}) {
            val thread = Thread(Runnable {
                val success = downloadUrlToFileSync(activity, urlString, fileName)
                handler(success)
            })
            thread.start()
        }
    }

}