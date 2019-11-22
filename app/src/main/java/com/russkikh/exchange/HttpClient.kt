package com.russkikh.exchange

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

 class HttpClient {

    val client = OkHttpClient()
    val JSON = "application/json; charset=utf-8".toMediaType()
    fun post(url: String, parameters: JSONObject, callback: Callback): Call {
        println(parameters.toString().toRequestBody())
        val request = Request.Builder()
            .url(url)
            .post(parameters.toString().toRequestBody(JSON))
            .build()


        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    fun post_w_auth(url: String, jwt:String , parameters: JSONObject, callback: Callback): Call {
         println(parameters.toString().toRequestBody())
         val request = Request.Builder()
             .url(url)
             .post(parameters.toString().toRequestBody(JSON))
             .build()


         val call = client.newCall(request)
         call.enqueue(callback)
         return call
     }

    fun get(url: String, jwt:String, callback: Callback): Call {
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", jwt)
            .build()

        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }
}