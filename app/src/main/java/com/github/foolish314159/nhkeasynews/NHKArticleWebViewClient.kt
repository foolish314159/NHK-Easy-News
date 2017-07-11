package com.github.foolish314159.nhkeasynews

import android.net.Uri
import android.os.Build
import android.support.annotation.RequiresApi
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import java.net.URI
import java.net.URL

/**
 * Custom WebViewClient
 * Opens URL links in same view
 * @property dictionary [Dictionary] to use for translation
 */
class NHKArticleWebViewClient(val dictionary: Dictionary) : WebViewClient() {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        request?.url?.lastPathSegment?.let {
            dictionary.translate(it)
        }
        return true
    }

    // Probably needed for version before API level 21?
    @Suppress("OverridingDeprecatedMember")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        url?.let {
            dictionary.translate(it.substring(it.lastIndexOf("/") + 1))
        }
        return true
    }
}