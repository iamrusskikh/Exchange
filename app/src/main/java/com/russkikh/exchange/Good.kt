package com.russkikh.exchange



class Good{
    var goodId:Int = -1
    lateinit var name:String
    lateinit var description:String
    var ownerId:Int = -1
    lateinit var change:String

    constructor(goodId:Int, name:String,description:String, ownerId:Int, chande:String) {
        this.goodId = goodId
        this.name = name
        this.description = description
        this.ownerId =  ownerId
        this.change = chande
    }

    constructor()
}