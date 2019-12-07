package com.russkikh.exchange

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.*
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject


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
        val loadingLayout =findViewById<FrameLayout>(R.id.loading)
        val httpClient = HttpClient.getInstance()
        val email = findViewById<EditText>(R.id.emailfield).text.toString().toLowerCase()
        val password = findViewById<EditText>(R.id.passwordfield).text.toString()
        val body = JSONObject()
        body.put("email", email)
        body.put("password", password)
        body.put("dormitoryId", dormitory_id)
        var response:String = ""
        loadingLayout.visibility = View.VISIBLE
        GlobalScope.launch {
            response = async(Dispatchers.IO) {httpClient.POST("/user/signup", body)}.await()
            delay(10)
            checkResponse(response)
        }
    }

    suspend fun checkResponse(data: String) {
        if (data.isEmpty()) {
            findViewById<FrameLayout>(R.id.loading).visibility = View.GONE
            runOnUiThread() {
                Toast.makeText(
                    baseContext, "user successfully registered",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        } else {
            try {
                val jsonObject = JSONObject(data)
                runOnUiThread() {
                    Toast.makeText(
                        baseContext, jsonObject.getString("message"),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: JSONException) {
                if (Regex("java").containsMatchIn(data))
                    runOnUiThread() {
                        Toast.makeText(
                            baseContext, "No connection to server",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }


    override fun onItemSelected(arg0: AdapterView<*>, arg1: View, position: Int, id: Long) {
        if(position==0)
            dormitory_id = 1
        else dormitory_id = position
    }

    override fun onNothingSelected(arg0: AdapterView<*>) {
    }

}
