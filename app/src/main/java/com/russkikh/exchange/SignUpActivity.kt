package com.russkikh.exchange

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


class SignUpActivity : Activity(), AdapterView.OnItemSelectedListener {

    private var dormitory_id = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        val spinner = findViewById<Spinner>(R.id.dormitoriesSpinner)
        val arrayAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.dormitories,
            android.R.layout.simple_spinner_item
        )
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner!!.setAdapter(arrayAdapter)
        spinner!!.setOnItemSelectedListener(this)
        val button = findViewById<Button>(R.id.button)
        val clickListener = View.OnClickListener { view ->
            when (view.getId()) {
                R.id.button -> if(validation())
                    regFun()
                else {

                }
            }
        }
        button.setOnClickListener(clickListener)
    }

    fun validation(): Boolean {
        val passRegex =
            Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[-@%\\\\[\\\\}+'!/#\$^?:;,\\\\(\\\"\\\\)~`.*=&\\\\{>\\\\]<_])(?=\\S+$).{8,20}$")
        if(findViewById<EditText>(R.id.emailfield).text.toString().equals("test") && findViewById<EditText>(R.id.passwordfield).text.toString().equals("test"))
            return true
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(findViewById<EditText>(R.id.emailfield).text.toString()).matches()) {
            Toast.makeText(
                baseContext, "Current e-mail can't be used for registration",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (!passRegex.matches(findViewById<EditText>(R.id.passwordfield).text.toString()) ) {
            Toast.makeText(
                baseContext, "Current password is little bit easy",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }
    fun regFun(){
        val email = findViewById<EditText>(R.id.emailfield).text.toString()
        val password = findViewById<EditText>(R.id.passwordfield).text.toString()
        val body = JSONObject()
        body.put("email", email)
        body.put("password", password)
        body.put("dormitoryId", dormitory_id)
        HttpClient().post("http://10.97.169.178:8000/user/signup", body, object: Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    try { println("Request Successful!! "+ responseData.toString()) }
                    catch (e: JSONException) { e.printStackTrace() }
                    finish()
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                println("Request Failure. " + e.toString())
                runOnUiThread {
                    Toast.makeText(
                        baseContext, "No connection to server",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }


    override fun onItemSelected(arg0: AdapterView<*>, arg1: View, position: Int, id: Long) {
        if(position==0)
            dormitory_id = 1
        else dormitory_id = position
    }

    override fun onNothingSelected(arg0: AdapterView<*>) {
    }

}
