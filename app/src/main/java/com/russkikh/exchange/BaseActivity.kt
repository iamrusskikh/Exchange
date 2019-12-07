package com.russkikh.exchange

import android.content.Intent
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button

class BaseActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        val feedButton = findViewById<Button>(R.id.feedButton)
        val newOfferButton = findViewById<Button>(R.id.newOfferButton)
        val profileButton = findViewById<Button>(R.id.profileButton)
        val tempChatButton = findViewById<Button>(R.id.testChatButton)
        val clickListener = View.OnClickListener { view ->
            when (view.getId()) {
                R.id.newOfferButton -> changeActivity(NewOfferActivity())
                R.id.feedButton -> changeActivity(FeedActivity())
                R.id.profileButton -> changeActivity(ProfileActivity())
                R.id.testChatButton ->changeActivity(ChatActivity())
            }
        }

        feedButton.setOnClickListener(clickListener)
        profileButton.setOnClickListener(clickListener)
        newOfferButton.setOnClickListener(clickListener)
    }

    private fun changeActivity(activity: Activity)
    {
        val intent = Intent(this, activity::class.java)
        startActivity(intent)
    }
}
