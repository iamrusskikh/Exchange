package com.russkikh.exchange.socketio_kotlin.parser

interface Parser {

    interface Encoder {

        fun encode(obj: Packet, callback: Callback)

        interface Callback {

            fun call(data: Array<Any>)
        }
    }

    interface Decoder {

        fun add(obj: String)

        fun add(obj: ByteArray)

        fun destroy()

        fun onDecoded(callback: Callback)

        interface Callback {

            fun call(packet: Packet)
        }
    }

}