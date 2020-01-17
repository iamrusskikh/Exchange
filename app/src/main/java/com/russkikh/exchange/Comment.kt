package com.russkikh.exchange

import java.sql.Timestamp

class Comment {
    var commentId:Int = -1
    var ownerId:Int = -1
    var goodId:Int = -1
    lateinit var content:String

    constructor(commentId:Int, ownerId:Int, goodId:Int, content:String) {
        this.commentId = commentId
        this.content = content
        this.goodId = goodId
        this.ownerId = ownerId
    }
    constructor()
}