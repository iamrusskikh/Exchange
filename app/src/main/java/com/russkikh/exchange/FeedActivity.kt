package com.russkikh.exchange

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class FeedActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)
        val feedButton = findViewById<Button>(R.id.feed_vector)
        val newOfferButton = findViewById<Button>(R.id.new_offer_vector)
        val profileButton = findViewById<Button>(R.id.profile_vector)
        val searchButton = findViewById<Button>(R.id.search_vector)
        val clickListener = View.OnClickListener { view ->
            when (view.getId()) {
                R.id.feed_vector -> update()
                R.id.search_vector ->changeActivity(SearchActivity())
                R.id.new_offer_vector -> changeActivity(NewOfferActivity())
                R.id.profile_vector ->changeActivity(ProfileActivity())
            }
        }

        feedButton.setOnClickListener(clickListener)
        profileButton.setOnClickListener(clickListener)
        newOfferButton.setOnClickListener(clickListener)
        searchButton.setOnClickListener(clickListener)
    }

    private fun changeActivity(activity: Activity)
    {
        val intent = Intent(this, activity::class.java)
        startActivity(intent)
    }

    fun update(){
        val loadingLayout =findViewById<FrameLayout>(R.id.loading)
        val httpClient = HttpClient.getInstance()
        loadingLayout.visibility = View.VISIBLE
        var listView_product: ListView
        var arrayList_products: ArrayList<Good> = ArrayList()
        listView_product = findViewById<ListView>(android.R.id.list) as ListView
        GlobalScope.launch {
            val response = async { httpClient.GET("/good", User.getInstance().token) }.await()
            delay(10)
            if (checkResponse(response))
                arrayList_products = async { parseResponse(response) }.await()
            delay(10)
            var good_adapter = ProductAdapter(this@FeedActivity, arrayList_products)
            delay(1000)
            runOnUiThread {
                findViewById<FrameLayout>(R.id.loading).visibility = View.GONE
                listView_product.adapter = good_adapter
            }
            listView_product.onItemClickListener = object : AdapterView.OnItemClickListener {
                override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    val itemValue = listView_product.getItemAtPosition(position) as Good
                    val intent = Intent(this@FeedActivity, ProductActivity::class.java)
                    intent.putExtra("productId",itemValue.goodId)
                    intent.putExtra("productDesc",itemValue.description)
                    intent.putExtra("productName", itemValue.name)
                    intent.putExtra("change",itemValue.change)
                    intent.putExtra("ownerId", itemValue.ownerId)
                    intent.putExtra("urgently", itemValue.urgently)
                    startActivity(intent)
                }
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

    override fun onResume() {
        super.onResume()
        update()
    }
    private suspend fun parseResponse(responseData: String?): ArrayList<Good> {
        val goodsJSONArray = JSONArray(responseData.toString())
        var arrayList_details: ArrayList<Good> = ArrayList();
        var size: Int = goodsJSONArray.length()
        try {
            for (i in 0..size - 1) {
                var JSONGoodDetail: JSONObject = goodsJSONArray.getJSONObject(i)
                var good: Good = Good();
                good.goodId = JSONGoodDetail.getInt("goodId")
                good.name = JSONGoodDetail.getString("name")
                good.description = JSONGoodDetail.getString("description")
                good.ownerId = JSONGoodDetail.getInt("ownerId")
                if(JSONGoodDetail.has("change"))
                    good.change = JSONGoodDetail.getString("change")
                else good.change = ""
                good.urgently = JSONGoodDetail.getBoolean("urgently")
                arrayList_details.add(good)
            }
        }catch (e:JSONException)
        {
            runOnUiThread {
                Toast.makeText(
                    baseContext, "Sth gone wrong",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return arrayList_details
    }
}
