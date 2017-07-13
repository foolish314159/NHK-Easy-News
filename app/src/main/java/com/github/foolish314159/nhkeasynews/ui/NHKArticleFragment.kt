package com.github.foolish314159.nhkeasynews.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.foolish314159.nhkeasynews.article.NHKArticle
import com.github.foolish314159.nhkeasynews.R
import com.github.foolish314159.nhkeasynews.translation.URLBasedDictionary
import kotlinx.android.synthetic.main.fragment_nhk_article.*

class NHKArticleFragment : Fragment() {

    companion object {
        const val ARG_ARTICLE = "argumentArticle"
    }

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
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_nhk_article, container, false)

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        this.articleWebView.webViewClient = NHKArticleWebViewClient(URLBasedDictionary.jisho(articleWebView))

        arguments?.getParcelable<NHKArticle>(ARG_ARTICLE)?.let { article ->
            article.loadArticleText(activity) { response ->
                this.articleWebView.settings.javaScriptEnabled = true
                this.articleWebView.loadData(response, "text/html", "utf-8")
                print(response)
            }
        }
    }

}
