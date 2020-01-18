package com.russkikh.exchange

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView

class ProductAdapter(context: Context, arrayListDetails:ArrayList<Good>) : BaseAdapter(){

    private val layoutInflater: LayoutInflater
    private val arrayListDetails:ArrayList<Good>
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
        val productItemAdapter: ProductItemAdapter
        if (convertView == null) {
            view = this.layoutInflater.inflate(R.layout.product_adapter, parent, false)
            productItemAdapter = ProductItemAdapter(view)
            view.tag = productItemAdapter
        } else {
            view = convertView
            productItemAdapter = view.tag as ProductItemAdapter
        }

        productItemAdapter.goodName.text = arrayListDetails.get(position).name
        productItemAdapter.goodDescription.text = arrayListDetails.get(position).description
        productItemAdapter.change.text = arrayListDetails.get(position).change
        if(arrayListDetails.get(position).urgently)
            view!!.setBackgroundColor(Color.rgb(207,255,220))
        return view
    }
}

class ProductItemAdapter(row: View?) {
    val goodName: TextView
    val goodDescription: TextView
    val change: TextView
    val linearLayout: LinearLayout

    init {
        this.change = row?.findViewById<TextView>(R.id.change) as TextView
        this.goodName = row?.findViewById<TextView>(R.id.goodName) as TextView
        this.goodDescription = row?.findViewById<TextView>(R.id.goodDescription) as TextView
        this.linearLayout = row?.findViewById<LinearLayout>(R.id.productLayout) as LinearLayout
    }
}