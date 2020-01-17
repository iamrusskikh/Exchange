package com.russkikh.exchange



class Good{
    var goodId:Int = -1
    lateinit var name:String
    lateinit var description:String
    var ownerId:Int = -1
    lateinit var change:String
    var urgently:Boolean = false

    constructor(goodId:Int, name:String, description:String, ownerId:Int, change:String, urgently:Boolean) {
        this.goodId = goodId
        this.name = name
        this.description = description
        this.ownerId =  ownerId
        this.change = change
        this.urgently = urgently
    }

    constructor()
}