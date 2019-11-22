package com.russkikh.exchange

import android.content.Context

class User {
    var id: Int = -1;
    lateinit var token:String
    lateinit var email:String
    companion object {
        private var instance: User? = null

        fun getInstance(): User {
            if (instance == null)
                instance = User()

            return instance!!
        }
    }
    private constructor(id:Int, token:String, email:String){
        this.id=id
        this.token=token
        this.email=email
    }
    private constructor()
}