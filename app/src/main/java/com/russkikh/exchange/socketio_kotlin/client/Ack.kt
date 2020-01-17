package com.russkikh.exchange.socketio_kotlin.client

interface Ack {

    fun call(vararg args: Any)

}