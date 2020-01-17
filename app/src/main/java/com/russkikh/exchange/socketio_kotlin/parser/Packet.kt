package com.russkikh.exchange.socketio_kotlin.parser


class Packet {

    var type = -1
    var id = -1
    var nsp: String = ""
    var data: Any? = null
    var attachments: Int = 0
    var query: String? = null

    constructor() {}

    constructor(type: Int) {
        this.type = type
    }

    constructor(type: Int, data: Any) {
        this.type = type
        this.data = data
    }
}