package com.github.foolish314159.nhkeasynews.ui

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.foolish314159.nhkeasynews.R
import com.github.foolish314159.nhkeasynews.article.NHKArticle
import com.github.foolish314159.nhkeasynews.article.NHKArticleLoader

class NHKArticleListFragment : Fragment() {

    private var listener: OnListFragmentInteractionListener? = null
    private var articles = ArrayList<NHKArticle>()
    private var adapter : NHKArticleListRecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val loader = NHKArticleLoader(activity)
        loader.articlesFromWeb { list ->
            articles.clear()
            articles.addAll(list)
            adapter?.notifyDataSetChanged()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_nhkarticlelist, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            val context = view.getContext()
            val recyclerView = view
            val layoutManager = LinearLayoutManager(context)
            recyclerView.layoutManager = layoutManager
            recyclerView.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))
            adapter = NHKArticleListRecyclerViewAdapter(activity, articles, listener)
            recyclerView.adapter = adapter
        }
        return view
    }

    override fun onResume() {
        super.onResume()

        if (activity is MainActivity) {
            (activity as MainActivity).currentFragment = this
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(item: NHKArticle)
    }

}
