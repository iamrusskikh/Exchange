package com.russkikh.exchange

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
        val loadingLayout =findViewById<FrameLayout>(R.id.loading)
        val strings = makeBetterString()
        val body = JSONObject()
        body.put("name", strings[0])
        body.put("description", strings[1])
        body.put("change", strings[2])
        val user = User.getInstance()
        body.put("ownerId", user.id)
        val httpClient = HttpClient.getInstance()
        loadingLayout.visibility = View.VISIBLE
        GlobalScope.launch {
            val response = async { httpClient.POST("/good", user.token, body) }.await()
            delay(10)
            if (checkResponse(response))
                runOnUiThread() {
                    findViewById<FrameLayout>(R.id.loading).visibility = View.GONE
                    Toast.makeText(
                        baseContext, "new offer successfully registered",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
        }
    }

    private fun validateData():Boolean {
        val strings = makeBetterString()
        val text = Regex("[a-zA-ZА-Яа-яёЁ\\s0-9]{4,}")
        if (!text.matches(strings[0]) || !text.matches(strings[1]) || !text.matches(strings[2])) {
            Toast.makeText(
                baseContext, "All fields should be filled",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    private suspend fun checkResponse(response: String):Boolean {
        if (Regex("java").containsMatchIn(response)) {
            runOnUiThread {
                Toast.makeText(
                    baseContext, "No connection to server",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return false
        }
        try{
            val error =JSONObject(response)
            runOnUiThread {
                Toast.makeText(
                    baseContext, error.getString("error"),
                    Toast.LENGTH_SHORT
                ).show()
            }
            return false
        }
        catch (e:JSONException){ }
        return true
    }

    private fun makeBetterString():ArrayList<String> {
        val strings: ArrayList<String> = ArrayList()
        val name = findViewById<EditText>(R.id.exProduct).text.toString()
        strings.add(Regex("[\\s]{2,}").replace(name, " "))
        val description = findViewById<EditText>(R.id.exProDescr).text.toString()
        strings.add(Regex("[\\s]{2,}").replace(description, " "))
        val change = findViewById<EditText>(R.id.reProduct).text.toString()
        strings.add(Regex("[\\s]{2,}").replace(change, " "))
        return strings
    }

    override fun onNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
