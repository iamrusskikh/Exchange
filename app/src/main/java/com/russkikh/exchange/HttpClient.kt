package com.russkikh.exchange

import android.app.Activity
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class HttpClient {

    companion object {
        private var instance: HttpClient? = null

        fun getInstance(): HttpClient {
            if (instance == null)
                instance = HttpClient()

            return instance!!
        }
    }

    var ip = "http://10.97.169.178:9000"
    private val client = OkHttpClient.Builder()
        .connectTimeout(500, TimeUnit.MILLISECONDS)
        .callTimeout(500, TimeUnit.MILLISECONDS).build()
    val JSON = "application/json; charset=utf-8".toMediaType()

    private suspend fun post(url: String, parameters: JSONObject, callback: Callback): Call {
        val request = Request.Builder()
            .url(url)
            .post(parameters.toString().toRequestBody(JSON))
            .build()


        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    private suspend fun post(
        url: String,
        jwt: String,
        parameters: JSONObject,
        callback: Callback
    ): Call {
        val request = Request.Builder()
            .url(url)
            .post(parameters.toString().toRequestBody(JSON))
            .build()
        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    private suspend fun get(url: String, jwt: String, callback: Callback): Call {
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", jwt)
            .build()

        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    suspend fun POST(command: String, body: JSONObject): String {
        var responseString: String = ""
        GlobalScope.async(Dispatchers.IO) {
            post(ip + command, body, object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body!!.string()
                    println("Request Successful!! " + responseData)
                    responseString = responseData
                }

                override fun onFailure(call: Call, e: IOException) {
                    responseString = e.toString()
                    println("Request Failure. " + responseString)
                }
            })
        }.await()
        delay(1000)
        return responseString
    }

    suspend fun GET(command: String, token: String): String {
        var responseData: String = ""
        GlobalScope.async(Dispatchers.IO) {
            get(ip + command, token, object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    responseData = response.body!!.string()
                    println("Request Successful!!")
                }

                override fun onFailure(call: Call, e: IOException) {
                    responseData = e.toString()
                    println("Request Failure. " + responseData)
                }
            })
        }.await()
        delay(1000)
        return responseData
    }

    suspend fun POST(command: String, token: String, body: JSONObject): String {
        var responseData: String = ""
       GlobalScope.async {
           post(ip + command, token, body, object : Callback {
               override fun onResponse(call: Call, response: Response) {
                   responseData = response.body!!.string()
               }

               override fun onFailure(call: Call, e: IOException) {
                   responseData = e.toString()
                   println("Request Failure. " + responseData)
               }
           })
       }.await()
        delay(1000)
        return responseData
    }
}