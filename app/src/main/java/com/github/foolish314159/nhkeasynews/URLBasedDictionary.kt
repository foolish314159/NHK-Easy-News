package com.github.foolish314159.nhkeasynews

import android.webkit.WebView

/**
 * Online dictionary based on a URL. Replaces vocabulary placeholder with the search word and opens URL in webView
 * @property webView WebView in which the online dictionary will be displayed
 * @property placeholderURL Dictionary URL including placeholder for vocabulary. Placeholder has to be %word%
 * @constructor Create new URLBasedDictionary using [webView] and [placeholderURL]
 */
class URLBasedDictionary(val webView : WebView, val placeholderURL : String) : Dictionary {

    private val PLACEHOLDER = "%word%"

    companion object {
        private val JISHO_URL = "http://jisho.org/search/%word%"
        private val TANGORIN_URL = "http://tangorin.com/dict.php?dict=general&s=%word%"

        /** Jisho.org dictionary **/
        fun jisho(webView: WebView): URLBasedDictionary {
            return URLBasedDictionary(webView, JISHO_URL)
        }

        /** tangorin.com dictionary **/
        fun tangorin(webView: WebView): URLBasedDictionary {
            return URLBasedDictionary(webView, TANGORIN_URL)
        }
    }

    override fun translate(word: String) {
        val formattedURL = placeholderURL.replace(PLACEHOLDER, word)
        webView.loadUrl(formattedURL)
    }

}