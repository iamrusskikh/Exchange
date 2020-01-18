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

class ProductActivity : Activity() {
    var httpClient = HttpClient.getInstance()
    var good:Good = Good()
    var user:User = User.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setActionBar(toolbar)
        getActionBar()?.setDisplayHomeAsUpEnabled(true)
        getActionBar()?.setHomeButtonEnabled(true)
        getActionBar()?.setDisplayShowTitleEnabled(false)
        toolbar.setTitle("Feed");
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorForTitles))
        good = Good(
            intent.getIntExtra("productId", -1), intent.getStringExtra("productName"),
            intent.getStringExtra("productDesc"), intent.getIntExtra("ownerId", -1),
            intent.getStringExtra("change"), intent.getBooleanExtra("urgently", false))
        val editButton = findViewById<Button>(R.id.editButton)
        val chatButton = findViewById<Button>(R.id.chatButton)
        val commentButton = findViewById<Button>(R.id.createCommentButton)
        val clickListener = View.OnClickListener { view ->
            when (view.getId()) {
                R.id.editButton ->editFun(good)
                R.id.chatButton ->chatFun()
                R.id.createCommentButton -> commentFun()
            }
        }
        editButton.setOnClickListener(clickListener)
        chatButton.setOnClickListener(clickListener)
        commentButton.setOnClickListener(clickListener)
        val user = User.getInstance()
        if(user.id == good.ownerId)
            editButton.visibility = View.VISIBLE
        else chatButton.visibility =View.VISIBLE
        findViewById<TextView>(R.id.goodName).text =good.name.toString()
        findViewById<TextView>(R.id.ownerName).text = good.ownerId.toString()
        var changeDescription = good.change
        if(changeDescription=="")
            changeDescription = "Ready for your offer"
        findViewById<TextView>(R.id.change).text = changeDescription
        findViewById<TextView>(R.id.goodDescription).text = good.description
    }

    override fun onResume() {
        super.onResume()
        update()
    }

    private fun commentFun()
    {
        val body:JSONObject = JSONObject()
        val content = findViewById<EditText>(R.id.comment).text.toString()
        body.put("content", content)
        body.put("ownerId", user.id)
        body.put ("goodId", good.goodId)
        val httpClient = HttpClient.getInstance()
        GlobalScope.launch {
            val response = async { httpClient.POST("/comment", user.token, body) }.await()
            delay(10)
            if (checkResponse(response))
                runOnUiThread() {
                    Toast.makeText(
                        baseContext, "new comment successfully registered",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun update() {
        var listView_product: ListView
        var arrayList_products: ArrayList<Comment> = ArrayList()
        listView_product = findViewById<ListView>(android.R.id.list) as ListView
        GlobalScope.launch {
            val response = async { httpClient.GET("/comment?goodId="+good.goodId, user.token) }.await()
            delay(10)
            if (checkResponse(response))
                arrayList_products = async { parseResponse(response) }.await()
            delay(10)
            var comments_adapter = CommentsAdapter(this@ProductActivity, arrayList_products)
            delay(10)
            runOnUiThread {
                listView_product.adapter = comments_adapter
            }

        }
    }

    private fun chatFun(){

    }

    private fun editFun(itemValue:Good){
        val intent = Intent(this, EditActivity::class.java)
        intent.putExtra("productId",itemValue.goodId)
        intent.putExtra("productDesc",itemValue.description)
        intent.putExtra("productName", itemValue.name)
        intent.putExtra("change",itemValue.change)
        startActivity(intent)
        finish()
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

    private suspend fun parseResponse(responseData: String?): ArrayList<Comment> {
        val goodsJSONArray = JSONArray(responseData.toString())
        var arrayList_details: ArrayList<Comment> = ArrayList();
        var size: Int = goodsJSONArray.length()
        try {
            for (i in 0..size - 1) {
                var JSONGoodDetail: JSONObject = goodsJSONArray.getJSONObject(i)
                var comment: Comment = Comment();
                comment.goodId = JSONGoodDetail.getInt("goodId")
                comment.commentId = JSONGoodDetail.getInt("commentId")
                comment.content = JSONGoodDetail.getString("content")
                comment.ownerId = JSONGoodDetail.getInt("ownerId")
                arrayList_details.add(comment)
            }
        }catch (e: JSONException)
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
    override fun onNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
