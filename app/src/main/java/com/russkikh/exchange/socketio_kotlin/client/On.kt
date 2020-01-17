package com.russkikh.exchange.socketio_kotlin.client

import io.socket.emitter.Emitter


object On {

    fun on(obj: Emitter, ev: String, fn: Emitter.Listener): Handle {
        obj.on(ev, fn)
        return object : Handle {
            override fun destroy() {
                obj.off(ev, fn)
            }
        }
    }

    interface Handle {

        fun destroy()
    }
}