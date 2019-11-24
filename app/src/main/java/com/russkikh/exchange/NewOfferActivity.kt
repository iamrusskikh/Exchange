package com.russkikh.exchange

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.Toolbar
import androidx.core.content.ContextCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class NewOfferActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_offer)
        val createOfferButton = findViewById<Button>(R.id.createOfferButton)
        val toolbar = findViewById<Toolbar>(R.id.toolbar);
        setActionBar(toolbar)
        getActionBar()?.setDisplayHomeAsUpEnabled(true);
        getActionBar()?.setHomeButtonEnabled(true);
        getActionBar()?.setDisplayShowTitleEnabled(false);
        toolbar.setTitle("Creating offer");
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorForTitles))
        val clickListener = View.OnClickListener { view ->
            when (view.getId()) {
                R.id.toolbar -> onBackPressed()
                R.id.createOfferButton -> if (validateData())
                    createOffer()
                else {
                }
            }
        }
        createOfferButton.setOnClickListener(clickListener)
    }

    fun createOffer() {
        val strings = makeBetterString()
        val body = JSONObject()
        body.put("name", strings[0])
        body.put("description", strings[1])
        body.put("change", strings[2])
        val user = User.getInstance()
        body.put("ownerId", user.id)
        HttpClient().post_w_auth("http:/10.97.169.178:8000/good",user.token, body, object: Callback {
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

    fun validateData():Boolean {
        val strings = makeBetterString()
        val text = Regex("[a-zA-ZА-Яа-яёЁ]{4,}")
        if (!text.matches(strings[0]) || !text.matches(strings[1]) || !text.matches(strings[2])) {
            Toast.makeText(
                baseContext, "All fields should be filled",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    fun makeBetterString():ArrayList<String> {
        val strings: ArrayList<String> = ArrayList()
        val name = findViewById<EditText>(R.id.exProduct).toString()
        strings.add(Regex("[\\s]{2,}").replace(name, " "))
        val description = findViewById<EditText>(R.id.exProDescr).toString()
        strings.add(Regex("[\\s]{2,}").replace(description, " "))
        val change = findViewById<EditText>(R.id.reProduct).toString()
        strings.add(Regex("[\\s]{2,}").replace(change, " "))
        return strings
    }

    override fun onNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
