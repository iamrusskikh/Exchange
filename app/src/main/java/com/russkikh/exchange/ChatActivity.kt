package com.russkikh.exchange

import android.app.Activity
import android.os.Bundle
import android.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import java.net.URISyntaxException
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T






class ChatActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setActionBar(toolbar)
        getActionBar()?.setDisplayHomeAsUpEnabled(true)
        getActionBar()?.setHomeButtonEnabled(true)
        getActionBar()?.setDisplayShowTitleEnabled(false)
        toolbar.setTitle("Chat");
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorForTitles))

    }

    override fun onNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
