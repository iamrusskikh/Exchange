package com.russkikh.exchange

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        val feedButton = findViewById<Button>(R.id.feedButton)
        val newOfferButton = findViewById<Button>(R.id.newOfferButton)
        val profileButton = findViewById<Button>(R.id.profileButton)
        val clickListener = View.OnClickListener { view ->
            when (view.getId()) {
                R.id.newOfferButton -> {
                    val intent = Intent(this, NewOfferActivity::class.java)
                    startActivity(intent)
                }
                R.id.feedButton ->{
                    val intent = Intent(this, FeedActivity::class.java)
                    startActivity(intent)
                }
                R.id.profileButton ->{
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        feedButton.setOnClickListener(clickListener)
        profileButton.setOnClickListener(clickListener)
        newOfferButton.setOnClickListener(clickListener)
    }
}
