package com.russkikh.exchange

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class CustomAdapter(context: Context,arrayListDetails:ArrayList<Good>) : BaseAdapter(){

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
            view = this.layoutInflater.inflate(R.layout.adapter_layout, parent, false)
            productItemAdapter = ProductItemAdapter(view)
            view.tag = productItemAdapter
        } else {
            view = convertView
            productItemAdapter = view.tag as ProductItemAdapter
        }
        val editButton = view!!.findViewById<Button>(R.id.editButton)
        val chatButton = view!!.findViewById<Button>(R.id.chatButton)

        val clickListener = View.OnClickListener { view ->
            when (view.getId()) {
                R.id.editButton -> edit(position)
               // R.id.chatButton -> chat()
            }
        }
        editButton.setOnClickListener(clickListener)
        chatButton.setOnClickListener(clickListener)

        productItemAdapter.goodName.text = arrayListDetails.get(position).name
        productItemAdapter.goodDescription.text = arrayListDetails.get(position).description
        productItemAdapter.change.text = arrayListDetails.get(position).change
        return view
    }

    private fun edit(position: Int) {
        val item = getItem(position) as Good
        val intent = Intent(activity, EditActivity::class.java)
        intent.putExtra("productId",item.goodId)
        intent.putExtra("productDesc",item.description)
        intent.putExtra("productName", item.name)
        intent.putExtra("change",item.change)
        context.startActivity(intent)
    }
}

class ProductItemAdapter(row: View?) {
    val goodName: TextView
    val goodDescription: TextView
    val change: TextView
    val linearLayout: LinearLayout
    val editButton: Button
    val chatButton: Button

    init {
        this.change = row?.findViewById<TextView>(R.id.change) as TextView
        this.goodName = row?.findViewById<TextView>(R.id.goodName) as TextView
        this.goodDescription = row?.findViewById<TextView>(R.id.goodDescription) as TextView
        this.linearLayout = row?.findViewById<LinearLayout>(R.id.linearLayout) as LinearLayout
        this.editButton = row?.findViewById<Button>(R.id.editButton) as Button
        this.chatButton = row?.findViewById<Button>(R.id.chatButton) as Button
    }
}