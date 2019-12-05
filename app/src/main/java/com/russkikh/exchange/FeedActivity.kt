package com.russkikh.exchange

import android.app.Activity
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import android.widget.Toolbar
import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


class FeedActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)
        val httpClient = HttpClient.getInstance()
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
        var listView_details: ListView
        var arrayList_details: ArrayList<Good> = ArrayList()
        listView_details = findViewById<ListView>(android.R.id.list) as ListView
        GlobalScope.launch {
            val response = async { httpClient.GET("/good", User.getInstance().token) }.await()
            delay(10)
            if (checkResponse(response))
                arrayList_details = async { parseResponse(response) }.await()
            delay(100)
            runOnUiThread {
                val good_adapter: CustomAdapter
                good_adapter = CustomAdapter(applicationContext, arrayList_details)
                listView_details.adapter = good_adapter
            }
        }
    }
        override fun onNavigateUp(): Boolean {
            onBackPressed()
            return true
        }

        private fun checkResponse(response: String):Boolean {
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
