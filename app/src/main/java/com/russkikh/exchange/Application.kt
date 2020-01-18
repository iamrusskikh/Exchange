package com.russkikh.exchange

import android.app.Application
import android.util.Log
import com.russkikh.exchange.socketio_kotlin.extensions.EVENT
import io.socket.client.IO
import io.socket.client.Socket

class MySocketApp: Application() {


    lateinit var mSocket: Socket


    override fun onCreate() {
        super.onCreate()

        try {
            mSocket = IO.socket(HttpClient.getInstance().ip+"?token="+User.getInstance().token+"&"+"userId="+28)
        } catch (e: Exception) {
            Log.e("SOCKET", "Socket init")
        }

    }

    fun getSocketInstance() = mSocket
}