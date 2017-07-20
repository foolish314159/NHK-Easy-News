package com.github.foolish314159.nhkeasynews.ui

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.facebook.drawee.view.SimpleDraweeView
import com.github.foolish314159.nhkeasynews.R
import com.github.foolish314159.nhkeasynews.article.NHKArticle
import com.github.foolish314159.nhkeasynews.ui.NHKArticleListFragment.OnListFragmentInteractionListener
import se.fekete.furiganatextview.FuriganaView
import se.fekete.furiganatextview.utils.FuriganaUtils

class NHKArticleListRecyclerViewAdapter(private val activity: Activity, private val values: List<NHKArticle>, private val listener: OnListFragmentInteractionListener?) : RecyclerView.Adapter<NHKArticleListRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_nhkarticleitem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item = values[position]
        val rubyText = holder.item?.title
        holder.contentView.updateText(FuriganaUtils.parseRuby(rubyText))
        val uri = holder.item?.imageUri(activity)
        holder.imageView.setImageURI(uri)

        holder.view.setOnClickListener {
            holder.item?.let {
                listener?.onListFragmentInteraction(it)
            }
        }
    }

    override fun getItemCount(): Int {
        return values.size
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val contentView: FuriganaView = view.findViewById<View>(R.id.item_articleTitle) as FuriganaView
        val imageView: SimpleDraweeView = view.findViewById<View>(R.id.item_articleImage) as SimpleDraweeView
        var item: NHKArticle? = null
    }
}
