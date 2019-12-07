package com.russkikh.exchange

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject


class FeedActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)
        val httpClient = HttpClient.getInstance()
        val loadingLayout =findViewById<FrameLayout>(R.id.loading)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setActionBar(toolbar)
        getActionBar()?.setDisplayHomeAsUpEnabled(true)
        getActionBar()?.setHomeButtonEnabled(true)
        getActionBar()?.setDisplayShowTitleEnabled(false)
        toolbar.setTitle("Feed");
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorForTitles))
        val clickListener = View.OnClickListener { view ->
            when (view.getId()) {
                R.id.toolbar -> onBackPressed()
            }
        }
        loadingLayout.visibility = View.VISIBLE
        var listView_product: ListView
        var arrayList_products: ArrayList<Good> = ArrayList()
        listView_product = findViewById<ListView>(android.R.id.list) as ListView
        GlobalScope.launch {
            val response = async { httpClient.GET("/good", User.getInstance().token) }.await()
            delay(10)
            if (checkResponse(response))
                arrayList_products = async { parseResponse(response) }.await()
            delay(100)
            runOnUiThread {
                findViewById<FrameLayout>(R.id.loading).visibility = View.GONE
                val good_adapter = CustomAdapter(this@FeedActivity, arrayList_products)
                listView_product.adapter = good_adapter
            }

            listView_product.onItemClickListener = object : AdapterView.OnItemClickListener {

                override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    val itemValue = listView_product.getItemAtPosition(position) as Good
                    val itemViev = listView_product.getChildAt(position)
                    val chatButton = itemViev.findViewById<Button>(R.id.chatButton)
                    val editButton = itemViev.findViewById<Button>(R.id.editButton)
                    for(i in 0..listView_product.adapter.count-1)
                    {
                        val v:View = listView_product.getChildAt(i)
                        v.findViewById<Button>(R.id.editButton).visibility = View.GONE
                        v.findViewById<Button>(R.id.chatButton).visibility = View.GONE

                    }
                    if(User.getInstance().id==itemValue.ownerId)
                        changeVisibility(editButton)
                    else changeVisibility(chatButton)
                }
            }
        }
    }

    private fun changeVisibility(view: View) {
        when (view.visibility) {
            View.VISIBLE -> {
                view.visibility = View.GONE
            }
            View.GONE -> {
                view.visibility = View.VISIBLE
            }
        }

    }

    override fun onNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun checkResponse(response: String): Boolean {
        if (Regex("java").containsMatchIn(response)) {
            runOnUiThread {
                Toast.makeText(
                    baseContext, "No connection to server",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return false
        }
        return true
    }

    private suspend fun parseResponse(responseData: String?): ArrayList<Good> {
        val goodsJSONArray = JSONArray(responseData.toString())
        var arrayList_details: ArrayList<Good> = ArrayList();
        var size: Int = goodsJSONArray.length()
        for (i in 0..size - 1) {
            var JSONGoodDetail: JSONObject = goodsJSONArray.getJSONObject(i)
            var good: Good = Good();
            good.goodId = JSONGoodDetail.getInt("goodId")
            good.name = JSONGoodDetail.getString("name")
            good.description = JSONGoodDetail.getString("description")
            good.ownerId = JSONGoodDetail.getInt("ownerId")
            good.change = JSONGoodDetail.getString("change")
            arrayList_details.add(good)
        }
        return arrayList_details
    }
}
