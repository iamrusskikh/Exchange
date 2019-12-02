package com.russkikh.exchange

import android.app.Activity
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

 class HttpClient {

     companion object {
         private var instance: HttpClient? = null

         fun getInstance(): HttpClient {
             if (instance == null)
                 instance = HttpClient()

             return instance!!
         }
     }

     var ip = "http://10.97.169.178:8000"
     val client = OkHttpClient()
     val JSON = "application/json; charset=utf-8".toMediaType()
     suspend fun post(url: String, parameters: JSONObject, callback: Callback): Call {
         println(parameters.toString().toRequestBody())
         val request = Request.Builder()
             .url(url)
             .post(parameters.toString().toRequestBody(JSON))
             .build()


         val call = client.newCall(request)
         call.enqueue(callback)
         return call
     }

     /*suspend*/ fun post_w_auth(
         url: String,
         jwt: String,
         parameters: JSONObject,
         callback: Callback
     ): Call {
         println(parameters.toString().toRequestBody())
         val request = Request.Builder()
             .url(url)
             .post(parameters.toString().toRequestBody(JSON))
             .build()


         val call = client.newCall(request)
         call.enqueue(callback)
         return call
     }

     /*suspend*/ fun get(url: String, jwt: String, callback: Callback): Call {
         val request = Request.Builder()
             .url(url)
             .addHeader("Authorization", jwt)
             .build()

         val call = client.newCall(request)
         call.enqueue(callback)
         return call
     }

     suspend fun POST(command: String, body: JSONObject, activity: Activity):String{
         var responseString :String = ""
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
 }