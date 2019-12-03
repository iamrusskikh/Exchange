package com.russkikh.exchange

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.Toolbar
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_sign_in.*

class NewIPActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_ip)
        val toolbar = findViewById<Toolbar>(R.id.toolbar);
        val ChangeIPButton =findViewById<Button>(R.id.ChangeIPButton)
        setActionBar(toolbar)
        getActionBar()?.setDisplayHomeAsUpEnabled(true);
        getActionBar()?.setHomeButtonEnabled(true);
        getActionBar()?.setDisplayShowTitleEnabled(false);
        toolbar.setTitle("Type your IP");
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorForTitles))
        val clickListener = View.OnClickListener { view ->
            when (view.getId()) {
                R.id.toolbar -> onBackPressed()
                R.id.ChangeIPButton -> changeIP()
            }
        }
        ChangeIPButton.setOnClickListener(clickListener)
    }
    override fun onNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun changeIP() {
        val ip = findViewById<EditText>(R.id.IPField).toString()
        if(!android.util.Patterns.IP_ADDRESS.matcher(ip).matches())
            HttpClient.getInstance().ip = "http" + ip
        else Toast.makeText(
            baseContext, "You type incorrect IP-address",
            Toast.LENGTH_SHORT
        ).show()
    }
}
