package com.github.foolish314159.nhkeasynews.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.foolish314159.nhkeasynews.article.NHKArticle
import com.github.foolish314159.nhkeasynews.R
import com.github.foolish314159.nhkeasynews.translation.URLBasedDictionary
import com.github.foolish314159.nhkeasynews.util.HTTPRequestHelper
import com.github.foolish314159.nhkeasynews.util.URLHelper
import kotlinx.android.synthetic.main.fragment_nhk_article.*
import java.net.URLEncoder

class NHKArticleFragment : Fragment() {

    companion object {
        const val ARG_ARTICLE = "argumentArticle"
    }

    private var article: NHKArticle? = null
    private var articleText: String? = null
    private var loaded = false
    private var audioLoaded = false

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

        audioLoaded = false
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
        if (!audioLoaded) {
            article?.let {
                activity?.let { act ->
                    if (it.hasLocalAudio(act)) {
                        audioLoaded = true
                        articleAudioPlayer.setupPlayer(it.soundUri(act))
                    } else {
                        (act.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)?.let { manager ->
                            manager.activeNetworkInfo?.let { info ->
                                if (info.isConnected) {
                                    val fileName = it.articleId
                                    HTTPRequestHelper.downloadUrlToFileAsync(act, URLHelper.articleAudioURL(it.articleId), "$fileName.mp3") { success ->
                                        if (success) {
                                            audioLoaded = true
                                            articleAudioPlayer.setupPlayer(it.soundUri(act))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadArticle(text: String) {
        if (!loaded) {
            this.articleWebView?.settings?.javaScriptEnabled = true
            // TODO: why does this not work when offline? a
            //this.articleWebView?.loadData(URLEncoder.encode(text).replace("\\+", " "), "text/html", "utf-8")
            this.articleWebView?.loadDataWithBaseURL(null, text, "text/html", "utf-8", null)
            loaded = true
        }
    }

}
