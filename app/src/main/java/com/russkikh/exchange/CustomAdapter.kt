package com.russkikh.exchange

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView

class CustomAdapter(context: Context,arrayListDetails:ArrayList<Good>) : BaseAdapter(){

    private val layoutInflater: LayoutInflater
    private val arrayListDetails:ArrayList<Good>

    init {
        this.layoutInflater = LayoutInflater.from(context)
        this.arrayListDetails=arrayListDetails
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
        val listRowHolder: ListRowHolder
        if (convertView == null) {
            view = this.layoutInflater.inflate(R.layout.adapter_layout, parent, false)
            listRowHolder = ListRowHolder(view)
            view.tag = listRowHolder
        } else {
            view = convertView
            listRowHolder = view.tag as ListRowHolder
        }

        listRowHolder.goodName.text = arrayListDetails.get(position).name
        listRowHolder.goodDescription.text = arrayListDetails.get(position).description
        listRowHolder.change.text = arrayListDetails.get(position).change
        return view
    }
}

private class ListRowHolder(row: View?) {
    public val goodName: TextView
    public val goodDescription: TextView
    public val change: TextView
    public val linearLayout: LinearLayout

    init {
        this.change = row?.findViewById<TextView>(R.id.change) as TextView
        this.goodName = row?.findViewById<TextView>(R.id.goodName) as TextView
        this.goodDescription = row?.findViewById<TextView>(R.id.goodDescription) as TextView
        this.linearLayout = row?.findViewById<LinearLayout>(R.id.linearLayout) as LinearLayout
    }
}