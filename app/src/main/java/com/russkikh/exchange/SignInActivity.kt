package com.russkikh.exchange

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        val InButton = findViewById<Button>(R.id.InButton)
        val UpButton = findViewById<Button>(R.id.UpButton)

            val clickListener = View.OnClickListener { view ->
                when (view.getId()) {
                    R.id.UpButton -> regFun()
                    R.id.InButton -> if(emailValidation())
                        authFun()
                    else {

                    }
                }
            }

            InButton.setOnClickListener(clickListener)
            UpButton.setOnClickListener(clickListener)
    }

    fun emailValidation():Boolean{
        if(findViewById<EditText>(R.id.emailfield).text.toString().equals("test"))
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
        var user:User = User.getInstance()
        val intent = Intent(this, FeedActivity::class.java)
        val email = findViewById<EditText>(R.id.emailfield).text.toString()
        val password = findViewById<EditText>(R.id.passwordfield).text.toString()
        val body = JSONObject()
        body.put("email", email)
        body.put("password", password)
        HttpClient().post("http://10.97.169.178:8000/user/signin", body, object: Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body!!.string()
                runOnUiThread {
                    try {
                        println("Request Successful!! "+ responseData)
                        val userJSON = JSONObject(responseData)
                        user.id = userJSON.getInt("user_id")
                        user.email = body.getString("email")
                        user.token = userJSON.getString("token")
                        startActivity(intent)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            }
                override fun onFailure(call: Call, e: IOException) {
                    println("Request Failure. " + e.toString())
                    runOnUiThread() {
                        Toast.makeText(
                            baseContext, "No connection to server",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
    }

    private fun regFun() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    private var back_pressed: Long = 0

    override fun onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis())
            super.onBackPressed()
        else
            Toast.makeText(
                baseContext, "Press once again to exit!",
                Toast.LENGTH_SHORT
            ).show()
        back_pressed = System.currentTimeMillis()
    }
}