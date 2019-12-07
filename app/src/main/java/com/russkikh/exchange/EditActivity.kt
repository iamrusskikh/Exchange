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
import org.json.JSONException
import org.json.JSONObject


class EditActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setActionBar(toolbar)
        getActionBar()?.setDisplayHomeAsUpEnabled(true)
        getActionBar()?.setHomeButtonEnabled(true)
        getActionBar()?.setDisplayShowTitleEnabled(false)
        toolbar.setTitle("Editing offer");
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorForTitles))
        findViewById<EditText>(R.id.editReProduct).setText(intent.getStringExtra("change").toString())
        findViewById<EditText>(R.id.editExProduct).setText(intent.getStringExtra("productName").toString())
        findViewById<EditText>(R.id.editExProDescr).setText(intent.getStringExtra("productDesc").toString())
        val saveChangeButton = findViewById<Button>(R.id.editOfferButton)
        val deleteOfferButton = findViewById<Button>(R.id.deleteOfferButton)
        val clickListener = View.OnClickListener { view ->
            when (view.getId()) {
                R.id.toolbar -> onBackPressed()
                R.id.editOfferButton -> if (validateData()) editFun()
                R.id.deleteOfferButton -> deleteFun()
            }
        }
        saveChangeButton.setOnClickListener(clickListener)
        deleteOfferButton.setOnClickListener(clickListener)
    }

    private fun deleteFun() {
        val body = JSONObject()
        body.put("goodId", intent.getIntExtra("productId", -1))
        val httpClient = HttpClient.getInstance()
        val user = User.getInstance()
        GlobalScope.launch {
            val response = async { httpClient.DELETE("/good", user.token, body) }.await()
            delay(10)
            if (checkResponse(response))
                runOnUiThread() {
                    Toast.makeText(
                        baseContext, "offer successfully deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
        }
    }

    private fun validateData(): Boolean {
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

    private fun makeBetterString(): ArrayList<String> {
        val strings: ArrayList<String> = ArrayList()
        val name = findViewById<EditText>(R.id.editExProduct).text.toString()
        strings.add(Regex("[\\s]{2,}").replace(name, " "))
        val description = findViewById<EditText>(R.id.editExProDescr).text.toString()
        strings.add(Regex("[\\s]{2,}").replace(description, " "))
        val change = findViewById<EditText>(R.id.editReProduct).text.toString()
        strings.add(Regex("[\\s]{2,}").replace(change, " "))
        return strings
    }


    private fun editFun() {
        val strings: ArrayList<String> = makeBetterString()
        val body = JSONObject()
        body.put("name", strings[0])
        body.put("description", strings[1])
        body.put("change", strings[2])
        body.put("goodId", intent.getIntExtra("productId", -1))
        val httpClient = HttpClient.getInstance()
        val user = User.getInstance()
        GlobalScope.launch {
            val response = async { httpClient.PATCH("/good", user.token, body) }.await()
            delay(10)
            if (checkResponse(response))
                runOnUiThread() {
                    Toast.makeText(
                        baseContext, "offer successfully edited",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
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
        catch (e: JSONException){ }
        return true
    }
}

