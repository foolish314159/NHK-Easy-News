package com.github.foolish314159.nhkeasynews

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onBackPressed() {
        // Back navigation from dictionary
        if (this.articleWebView.canGoBack()) {
            this.articleWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.articleWebView.webViewClient = MyWebViewClient()

        val testArticle = NHKArticle("k10011047501000", false)
        testArticle.loadArticleText(this) { response ->
            if (response != null) {
                print(response)
                this.articleWebView.settings.javaScriptEnabled = true
                this.articleWebView.loadData(response, "text/html", "utf-8")
            }
        }
    }
}
