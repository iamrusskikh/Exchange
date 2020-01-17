package com.russkikh.exchange

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView

class CommentsAdapter(context: Context, arrayListDetails:ArrayList<Comment>) : BaseAdapter(){

    private val layoutInflater: LayoutInflater
    private val arrayListDetails:ArrayList<Comment>
    private val activity:Activity
    private val context:Context

    init {
        this.layoutInflater = LayoutInflater.from(context)
        this.arrayListDetails=arrayListDetails
        this.activity = context as Activity
        this.context = context
    }

    override fun getCount(): Int {
        return arrayListDetails.size
    }

    override fun getItem(position: Int): Any {
        return arrayListDetails.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val view: View?
        val commentsItemAdapter: CommentsItemAdapter
        if (convertView == null) {
            view = this.layoutInflater.inflate(R.layout.comment_adapter, parent, false)
            commentsItemAdapter = CommentsItemAdapter(view)
            view.tag = commentsItemAdapter
        } else {
            view = convertView
            commentsItemAdapter = view.tag as CommentsItemAdapter
        }

        commentsItemAdapter.onwerName.text = arrayListDetails.get(position).ownerId.toString()
        commentsItemAdapter.content.text = arrayListDetails.get(position).content
        return view
    }
}

class CommentsItemAdapter(row: View?) {
    val onwerName: TextView
    val content: TextView
    val linearLayout: LinearLayout

    init {
        this.onwerName = row?.findViewById<TextView>(R.id.ownerName) as TextView
        this.content = row?.findViewById<TextView>(R.id.content) as TextView
        this.linearLayout = row?.findViewById<LinearLayout>(R.id.commentLayout) as LinearLayout
    }
}