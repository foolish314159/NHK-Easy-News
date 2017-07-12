package com.github.foolish314159.nhkeasynews

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.github.foolish314159.nhkeasynews.NHKArticleListFragment.OnListFragmentInteractionListener

class NHKArticleListRecyclerViewAdapter(private val mValues: List<NHKArticle>, private val mListener: OnListFragmentInteractionListener?) : RecyclerView.Adapter<NHKArticleListRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_nhkarticleitem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        holder.mIdView.text = holder.mItem?.articleId
        holder.mContentView.text = "Test 123"
        holder.mView.setBackgroundColor(if (position % 2 == 0) Color.LTGRAY else Color.WHITE)

        holder.mView.setOnClickListener {
            holder.mItem?.let {
                mListener?.onListFragmentInteraction(it)
            }
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.findViewById<View>(R.id.id) as TextView
        val mContentView: TextView = mView.findViewById<View>(R.id.content) as TextView
        var mItem: NHKArticle? = null

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
