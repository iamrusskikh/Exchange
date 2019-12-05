package com.russkikh.exchange

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
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
                R.id.newOfferButton -> regFun()
                R.id.feedButton -> if (emailValidation())
                    authFun()
                else {

                }
                R.id.NewIPButton -> changeIP()
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
        val progressBar =findViewById<ProgressBar>(R.id.progress_circular)
        val user: User = User.getInstance()
        val email = findViewById<EditText>(R.id.emailfield).text.toString().toLowerCase()
        val password = findViewById<EditText>(R.id.passwordfield).text.toString()
        val body = JSONObject()
        body.put("email", email)
        body.put("password", password)
        val httpClient = HttpClient.getInstance()
        progressBar.visibility = View.VISIBLE
        GlobalScope.launch {
            val data = async (Dispatchers.IO){ httpClient.POST("/user/signin", body)}.await()
            delay(10)
            async {getUpdate(data)}.await()
            delay(10)
            user.email = email
            goToBase()
        }
    }
    private fun changeIP() {
        val intent = Intent(this, NewIPActivity::class.java)
        startActivity(intent)
    }

    private fun goToBase(){
        val intent = Intent(this, BaseActivity::class.java)
        if (User.getInstance().id != -1)
            startActivity(intent)
        runOnUiThread(){
            findViewById<ProgressBar>(R.id.progress_circular).visibility = View.GONE
        }
    }

    private fun regFun() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
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
            user.token = jsonWData.getString("token")
            user.id = jsonWData.getInt("user_id")
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