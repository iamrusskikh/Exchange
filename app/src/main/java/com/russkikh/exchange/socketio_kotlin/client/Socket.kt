package com.russkikh.exchange.socketio_kotlin.client


import android.util.Log
import io.socket.emitter.Emitter
import io.socket.emitter.Emitter.Listener
import io.socket.thread.EventThread
import com.russkikh.exchange.socketio_kotlin.extensions.*
import com.russkikh.exchange.socketio_kotlin.parser.Packet
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*


/**
 * The socket class for Socket.IO Client.
 */
class Socket(private val io: Manager, private val nsp: String, opts: Manager.Companion.Options?) : Emitter() {

    /*package*/ internal var id: String? = null

    @Volatile
    private var connected: Boolean = false
    private var ids: Int = 0
    private var query: String? = null
    private val acks = HashMap<Int, Ack>()
    private var subs: Queue<On.Handle>? = null
    private val receiveBuffer = LinkedList<List<Any>>()
    private val sendBuffer = LinkedList<Packet>()

    init {
        if (opts != null) {
            this.query = opts.query
        }
    }

    private fun subEvents() {
        if (this.subs != null) return

        val io = this@Socket.io
        this@Socket.subs = object : LinkedList<On.Handle>() {
            init {
                add(On.on(io, Manager.EVENT_OPEN, Listener { this@Socket.onopen() }))
                add(
                    On.on(io, Manager.EVENT_PACKET,
                        Listener { args -> this@Socket.onpacket(args[0] as Packet) })
                )
                add(
                    On.on(io, Manager.EVENT_CLOSE,
                        Listener { args ->
                            if (args.isNotEmpty()) {
                                this@Socket.onclose(args[0] as String)
                            }
                        })
                )
            }
        }
    }

    /**
     * Connects the socket.
     */
    fun open(): Socket {
        EventThread.exec(Runnable {
            if (this@Socket.connected) return@Runnable

            this@Socket.subEvents()
            this@Socket.io.open() // ensure open
            if (Manager.ReadyState.OPEN === this@Socket.io.readyState) this@Socket.onopen()
            this@Socket.emit(EVENT_CONNECTING)
        })
        return this
    }

    /**
     * Connects the socket.
     */
    fun connect(): Socket {
        return this.open()
    }

    /**
     * Send messages.
     *
     * @param args data to send.
     * @return a reference to this object.
     */
    fun send(vararg args: Any): Socket {
        EventThread.exec { this@Socket.emit(EVENT_MESSAGE, *args) }
        return this
    }

    /**
     * Emits an event. When you pass [Ack] at the last argument, then the acknowledge is done.
     *
     * @param event an event name.
     * @param args data to send.
     * @return a reference to this object.
     */
    override fun emit(event: String, vararg args: Any): Emitter {
        EventThread.exec(Runnable {
            if (events.containsKey(event)) {
                super@Socket.emit(event, *args)
                return@Runnable
            }

            val ack: Ack?
            val _args: Array<Any>
            val lastIndex = args.size - 1

            if (args.isNotEmpty() && args[lastIndex] is Ack) {
                _args = arrayOf<Any>(lastIndex)
                for (i in 0 until lastIndex) {
                    _args[i] = args[i]
                }
                ack = args[lastIndex] as Ack
            } else {
                _args = arrayOf(args)
                ack = null
            }

            emit(event, _args, ack)
        })
        return this
    }

    /**
     * Emits an event with an acknowledge.
     *
     * @param event an event name
     * @param args data to send.
     * @param ack the acknowledgement to be called
     * @return a reference to this object.
     */
    fun emit(event: String, args: Array<Any>?, ack: Ack?): Emitter {
        EventThread.exec {
            val jsonArgs = JSONArray()
            jsonArgs.put(event)

            if (args != null) {
                for (arg in args) {
                    jsonArgs.put(arg)
                }
            }

            val packet = Packet(EVENT, jsonArgs)

            if (ack != null) {
                Log.i(TAG, String.format("emitting packet with ack id %d", ids))
                this@Socket.acks[ids] = ack
                packet.id = ids++
            }

            if (this@Socket.connected) {
                this@Socket.packet(packet)
            } else {
                this@Socket.sendBuffer.add(packet)
            }
        }
        return this
    }

    private fun packet(packet: Packet) {
        packet.nsp = this.nsp
        this.io.packet(packet)
    }

    private fun onopen() {
        Log.i(TAG, "transport is open - connecting")

        if ("/" != this.nsp) {
            if (this.query != null && !this.query!!.isEmpty()) {
                val packet = Packet(CONNECT)
                packet.query = this.query
                this.packet(packet)
            } else {
                this.packet(Packet(CONNECT))
            }
        }
    }

    private fun onclose(reason: String) {
        if (Log.isLoggable(TAG, Log.INFO)) {
            Log.i(TAG, String.format("close (%s)", reason))
        }
        this.connected = false
        this.id = null
        this.emit(EVENT_DISCONNECT, reason)
    }

    private fun onpacket(packet: Packet) {
        if (this.nsp != packet.nsp) return

        when (packet.type) {
            CONNECT -> this.onconnect()

            EVENT -> {
                val p = packet
                this.onevent(p)
            }

            BINARY_EVENT -> {
                val p = packet
                this.onevent(p)
            }

            ACK -> {
                val p = packet
                this.onack(p)
            }

            BINARY_ACK -> {
                val p = packet
                this.onack(p)
            }

            DISCONNECT -> this.ondisconnect()

            ERROR -> packet.data?.let { this.emit(EVENT_ERROR, it) }
        }
    }

    private fun onevent(packet: Packet) {
        val args = ArrayList<Any>(Arrays.asList(toArray(packet.data as JSONArray)))
        if (Log.isLoggable(TAG, Log.INFO)) {
            Log.i(TAG, String.format("emitting event %s", args))
        }

        if (packet.id >= 0) {
            Log.i(TAG, "attaching ack callback to event")
            args.add(this.ack(packet.id))
        }

        if (this.connected) {
            if (args.isEmpty()) return
            val event = args.removeAt(0).toString()
            super.emit(event, *args.toTypedArray())
        } else {
            this.receiveBuffer.add(args)
        }
    }

    private fun ack(id: Int): Ack {
        val self = this
        val sent = booleanArrayOf(false)
        return object : Ack {
            override fun call(vararg args: Any) {
                EventThread.exec(Runnable {
                    if (sent[0]) return@Runnable
                    sent[0] = true
                    if (Log.isLoggable(TAG, Log.INFO)) {
                        Log.i(TAG, String.format("sending ack %s", if (args.isNotEmpty()) args else null))
                    }

                    val jsonArgs = JSONArray()
                    for (arg in args) {
                        jsonArgs.put(arg)
                    }

                    val packet = Packet(ACK, jsonArgs)
                    packet.id = id
                    self.packet(packet)
                })
            }
        }
    }

    private fun onack(packet: Packet) {
        val fn = this.acks.remove(packet.id)
        if (fn != null) {
            if (Log.isLoggable(TAG, Log.INFO)) {
                Log.i(TAG,String.format("calling ack %s with %s", packet.id, packet.data))
            }
            fn.call(toArray(packet.data as JSONArray))
        } else {
            if (Log.isLoggable(TAG, Log.INFO)) {
                Log.i(TAG, String.format("bad ack %s", packet.id))
            }
        }
    }

    private fun onconnect() {
        this.connected = true
        this.emit(EVENT_CONNECT)
        this.emitBuffered()
    }

    private fun emitBuffered() {
        var data: List<Any>? = this.receiveBuffer.poll()
        while (data != null) {
            data = this.receiveBuffer.poll()
            val event = data[0] as String
            super.emit(event, data.toTypedArray())
        }
        this.receiveBuffer.clear()

        var packet: Packet = this.sendBuffer.poll()
        while (packet != null) {
            packet = this.sendBuffer.poll()
            this.packet(packet)
        }
        this.sendBuffer.clear()
    }

    private fun ondisconnect() {
        if (Log.isLoggable(TAG, Log.INFO)) {
            Log.i(TAG, String.format("server disconnect (%s)", this.nsp))
        }
        this.destroy()
        this.onclose("io server disconnect")
    }

    private fun destroy() {
        if (this.subs != null) {
            // clean subscriptions to avoid reconnection
            for (sub in this.subs!!) {
                sub.destroy()
            }
            this.subs = null
        }

        this.io.destroy(this)
    }

    /**
     * Disconnects the socket.
     *
     * @return a reference to this object.
     */
    fun close(): Socket {
        EventThread.exec {
            if (this@Socket.connected) {
                if (Log.isLoggable(TAG, Log.INFO)) {
                    Log.i(TAG, String.format("performing disconnect (%s)", this@Socket.nsp))
                }
                this@Socket.packet(Packet(DISCONNECT))
            }

            this@Socket.destroy()

            if (this@Socket.connected) {
                this@Socket.onclose("io client disconnect")
            }
        }
        return this
    }

    /**
     * Disconnects the socket.
     *
     * @return a reference to this object.
     */
    fun disconnect(): Socket {
        return this.close()
    }

    fun io(): Manager {
        return this.io
    }

    fun connected(): Boolean {
        return this.connected
    }

    /**
     * A property on the socket instance that is equal to the underlying engine.io socket id.
     *
     * The value is present once the socket has connected, is removed when the socket disconnects and is updated if the socket reconnects.
     *
     * @return a socket id
     */
    fun id(): String? {
        return this.id
    }

    companion object {

        /**
         * Called on a connection.
         */
        val EVENT_CONNECT = "connect"

        val EVENT_CONNECTING = "connecting"

        /**
         * Called on a disconnection.
         */
        val EVENT_DISCONNECT = "disconnect"

        /**
         * Called on a connection error.
         *
         *
         * Parameters:
         *
         *  * (Exception) error data.
         *
         */
        val EVENT_ERROR = "error"

        val EVENT_MESSAGE = "message"

        val EVENT_CONNECT_ERROR = Manager.EVENT_CONNECT_ERROR

        val EVENT_CONNECT_TIMEOUT = Manager.EVENT_CONNECT_TIMEOUT

        val EVENT_RECONNECT = Manager.EVENT_RECONNECT

        val EVENT_RECONNECT_ERROR = Manager.EVENT_RECONNECT_ERROR

        val EVENT_RECONNECT_FAILED = Manager.EVENT_RECONNECT_FAILED

        val EVENT_RECONNECT_ATTEMPT = Manager.EVENT_RECONNECT_ATTEMPT

        val EVENT_RECONNECTING = Manager.EVENT_RECONNECTING

        val EVENT_PING = Manager.EVENT_PING

        val EVENT_PONG = Manager.EVENT_PONG

        protected var events: Map<String, Int> = object : HashMap<String, Int>() {
            init {
                put(EVENT_CONNECT, 1)
                put(EVENT_CONNECT_ERROR, 1)
                put(EVENT_CONNECT_TIMEOUT, 1)
                put(EVENT_CONNECTING, 1)
                put(EVENT_DISCONNECT, 1)
                put(EVENT_ERROR, 1)
                put(EVENT_RECONNECT, 1)
                put(EVENT_RECONNECT_ATTEMPT, 1)
                put(EVENT_RECONNECT_FAILED, 1)
                put(EVENT_RECONNECT_ERROR, 1)
                put(EVENT_RECONNECTING, 1)
                put(EVENT_PING, 1)
                put(EVENT_PONG, 1)
            }
        }

        private fun toArray(array: JSONArray): Array<Any> {
            val length = array.length()
            val data = arrayOfNulls<Any>(length)
            for (i in 0 until length) {
                var v: Any?
                try {
                    v = array.get(i)
                } catch (e: JSONException) {
                    Log.i(TAG, "An error occured while retrieving data from JSONArray", e)
                    v = null
                }

                data[i] = if (JSONObject.NULL.equals(v)) null else v
            }
            return arrayOf(data)
        }
    }
}

