package com.russkikh.exchange

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import org.json.JSONException
import org.json.JSONObject
import kotlinx.coroutines.*

class SignInActivity : Activity() {

    private var back_pressed: Long = 0

    override fun onResume() {
        super.onResume()
        findViewById<EditText>(R.id.emailfield).setText("")
        findViewById<EditText>(R.id.passwordfield).setText("")
        val button = findViewById<Button>(R.id.NewIPButton)
        button.isClickable = false
        button.visibility = View.GONE

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        val inButton = findViewById<Button>(R.id.feedButton)
        val upButton = findViewById<Button>(R.id.newOfferButton)
        val newIPButton = findViewById<Button>(R.id.NewIPButton)
        val clickListener = View.OnClickListener { view ->
            when (view.getId()) {
                R.id.newOfferButton -> changeActivity(SignUpActivity())
                R.id.feedButton -> if (emailValidation())
                    authFun()
                else {

                }
                R.id.NewIPButton -> changeActivity(NewIPActivity())
            }
        }

        inButton.setOnClickListener(clickListener)
        upButton.setOnClickListener(clickListener)
        newIPButton.setOnClickListener(clickListener)
    }

    private fun emailValidation(): Boolean {
        if (findViewById<EditText>(R.id.emailfield).text.toString().equals("test"))
            return true
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(findViewById<EditText>(R.id.emailfield).text.toString()).matches()) {
            Toast.makeText(
                baseContext, "Current e-mail can't be used for authorization",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }


    private fun authFun() {
        val loadingLayout =findViewById<FrameLayout>(R.id.loading)
        val user: User = User.getInstance()
        val email = findViewById<EditText>(R.id.emailfield).text.toString().toLowerCase()
        val password = findViewById<EditText>(R.id.passwordfield).text.toString()
        val body = JSONObject()
        body.put("email", email)
        body.put("password", password)
        val httpClient = HttpClient.getInstance()
        loadingLayout.visibility = View.VISIBLE
        GlobalScope.launch {
            val data = async (Dispatchers.IO){ httpClient.POST("/user/signin", body)}.await()
            delay(10)
            async {getUpdate(data)}.await()
            delay(10)
            user.email = email
            goToBase()
        }
    }

    private fun changeActivity(activity: Activity)
    {
        val intent = Intent(this, activity::class.java)
        startActivity(intent)
    }

    private fun goToBase(){
        runOnUiThread(){
            findViewById<FrameLayout>(R.id.loading).visibility = View.GONE
            if (User.getInstance().id != -1)
                changeActivity(FeedActivity())
        }
    }

    override fun onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()) {
            finishAffinity()
            System.exit(0)
        }
        else
            Toast.makeText(
                baseContext, "Press once again to exit!",
                Toast.LENGTH_SHORT
            ).show()
        back_pressed = System.currentTimeMillis()
    }

    suspend fun getUpdate(data:String) {
        var jsonWData = JSONObject()
        val user = User.getInstance()
        try {
            jsonWData = JSONObject(data)
            val jsonUser = jsonWData.getJSONObject("user")
            user.token = jsonWData.getString("token")
            user.id = jsonUser.getInt("userId")
            user.email = jsonUser.getString("email")
            val goods = jsonUser.getJSONArray("goods")
            var arrayList_details: ArrayList<Good> = ArrayList();
            var size: Int = goods.length()
            if (size!=0) {
                for (i in 0..size - 1) {
                    var JSONGoodDetail: JSONObject = goods.getJSONObject(i)
                    var good: Good = Good();
                    good.goodId = JSONGoodDetail.getInt("goodId")
                    good.name = JSONGoodDetail.getString("name")
                    good.description = JSONGoodDetail.getString("description")
                    good.ownerId = JSONGoodDetail.getInt("ownerId")
                    if(JSONGoodDetail.has("change"))
                        good.change = JSONGoodDetail.getString("change")
                    good.urgently = JSONGoodDetail.getBoolean("urgently")
                    arrayList_details.add(good)
                }
            }
            user.goods = arrayList_details
            user.dormitoryId =jsonUser.getInt("dormitoryId")
            user.name = jsonUser.getString("name")

        } catch (e: JSONException) {
            if (Regex("java").containsMatchIn(data)) {
                val button = findViewById<Button>(R.id.NewIPButton)
                runOnUiThread() {
                    Toast.makeText(
                        baseContext, "No connection to server",
                        Toast.LENGTH_SHORT
                    ).show()
                    button.isClickable = true
                    button.visibility = View.VISIBLE
                }
            } else runOnUiThread() {
                Toast.makeText(
                    baseContext, jsonWData.getString("message"),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}