package com.russkikh.exchange

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class User {
    var id: Int = -1
    var dormitoryId:Int = -1
    lateinit var token:String
    lateinit var email:String
    lateinit var name:String
    lateinit var goods:ArrayList<Good>

    companion object {
        private var instance: User? = null

        fun getInstance(): User {
            if (instance == null)
                instance = User()

            return instance!!
        }
    }
    private constructor(id:Int, token:String, email:String, dormitoryId:Int, name:String, goods:ArrayList<Good>){
        this.id = id
        this.token = token
        this.email = email
        this.dormitoryId = dormitoryId
        this.name = name
        this.goods = goods
    }
    private constructor()
}