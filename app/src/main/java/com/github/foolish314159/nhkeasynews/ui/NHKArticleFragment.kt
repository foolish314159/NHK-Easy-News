package com.github.foolish314159.nhkeasynews.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.foolish314159.nhkeasynews.article.NHKArticle
import com.github.foolish314159.nhkeasynews.R
import com.github.foolish314159.nhkeasynews.translation.URLBasedDictionary
import com.github.foolish314159.nhkeasynews.util.URLHelper
import kotlinx.android.synthetic.main.fragment_nhk_article.*

class NHKArticleFragment : Fragment() {

    companion object {
        const val ARG_ARTICLE = "argumentArticle"
    }

    private var article: NHKArticle? = null
    private var articleText: String? = null
    private var loaded = false

    /**
     * @return true if back press was handled
     */
    fun onBackPressed(): Boolean {
        // Back navigation from dictionary
        if (this.articleWebView.canGoBack()) {
            this.articleWebView.goBack()
            return true
        } else {
            return false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getParcelable<NHKArticle>(ARG_ARTICLE)?.let {
            article = it

            it.loadArticleText(activity) { response ->
                articleText = response

                // If view was created before article has been loaded, load into webview now
                if (!loaded) {
                    loadArticle(response)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_nhk_article, container, false)

        return view
    }

    override fun onResume() {
        super.onResume()

        loadAudio()
    }

    override fun onStop() {
        super.onStop()

        articleAudioPlayer.releasePlayer()
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        this.articleWebView.webViewClient = NHKArticleWebViewClient(URLBasedDictionary.jisho(articleWebView))

        // Try to load text into webview as soon as view has been created
        articleText?.let {
            loadArticle(it)
        }

        loadAudio()
    }

    private fun loadAudio() {
        // TODO: save .mp3 in internal storage

        article?.let {
            articleAudioPlayer.setupPlayer(URLHelper.articleAudioURL(it.articleId))
        }
    }

    private fun loadArticle(text: String) {
        this.articleWebView?.settings?.javaScriptEnabled = true
        this.articleWebView?.loadData(text, "text/html", "utf-8")
        loaded = true
    }

}
