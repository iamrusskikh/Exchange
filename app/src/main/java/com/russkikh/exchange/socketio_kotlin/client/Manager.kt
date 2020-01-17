package com.russkikh.exchange.socketio_kotlin.client


import io.socket.emitter.Emitter
import io.socket.emitter.Emitter.Listener
import io.socket.engineio.client.Socket.EVENT_DATA
import io.socket.thread.EventThread
import com.russkikh.exchange.socketio_kotlin.extensions.Backoff
import com.russkikh.exchange.socketio_kotlin.extensions.CONNECT
import com.russkikh.exchange.socketio_kotlin.extensions.SocketIOException
import com.russkikh.exchange.socketio_kotlin.parser.IOParser
import com.russkikh.exchange.socketio_kotlin.parser.Packet
import com.russkikh.exchange.socketio_kotlin.parser.Parser
import okhttp3.Call
import okhttp3.WebSocket
import java.net.URI
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger


/**
 * Manager class represents a connection to a given Socket.IO server.
 */
class Manager @JvmOverloads constructor(private val uri: URI, opts: Options? = null) : Emitter() {

    /*package*/ internal var readyState: ReadyState

    private var _reconnection: Boolean = false
    private var skipReconnect: Boolean = false
    private var reconnecting: Boolean = false
    private var encoding: Boolean = false
    private var _reconnectionAttempts: Int = 0
    private var _reconnectionDelay: Long = 0
    private var _reconnectionDelayMax: Long = 0
    private var _randomizationFactor: Double = 0.toDouble()
    private val backoff: Backoff?
    private var _timeout: Long = 0
    private val connecting = HashSet<Socket>()
    private var lastPing: Date? = null
    private val packetBuffer: MutableList<Packet>
    private val subs: Queue<On.Handle>
    private val opts: Options
    /*package*/ internal lateinit var engine: io.socket.engineio.client.Socket
    private val encoder: Parser.Encoder?
    private val decoder: Parser.Decoder?

    /**
     * This HashMap can be accessed from outside of EventThread.
     */
    /*package*/ internal var nsps: ConcurrentHashMap<String, Socket>

    /*package*/ internal enum class ReadyState {
        CLOSED, OPENING, OPEN
    }

//    constructor(opts: Options) : this(null, opts) {}

    init {
        var opts = opts
        if (opts == null) {
            opts = Options()
        }
        if (opts.path == null) {
            opts.path = "/socket.io"
        }
        if (opts.webSocketFactory == null) {
            opts.webSocketFactory = defaultWebSocketFactory
        }
        if (opts.callFactory == null) {
            opts.callFactory = defaultCallFactory
        }
        this.opts = opts
        this.nsps = ConcurrentHashMap<String, Socket>()
        this.subs = LinkedList<On.Handle>()
        this.reconnection(opts.reconnection)
        this.reconnectionAttempts(if (opts.reconnectionAttempts != 0) opts.reconnectionAttempts else Integer.MAX_VALUE)
        this.reconnectionDelay(if (opts.reconnectionDelay != 0L) opts.reconnectionDelay else 1000)
        this.reconnectionDelayMax(if (opts.reconnectionDelayMax != 0L) opts.reconnectionDelayMax else 5000)
        this.randomizationFactor(if (opts.randomizationFactor != 0.0) opts.randomizationFactor else 0.5)
        this.backoff = Backoff()
            .setMin(this.reconnectionDelay())
            .setMax(this.reconnectionDelayMax())
            .setJitter(this.randomizationFactor())
        this.timeout(opts.timeout)
        this.readyState = ReadyState.CLOSED
        this.encoding = false
        this.packetBuffer = ArrayList<Packet>()
        this.encoder = if (opts.encoder != null) opts.encoder else IOParser.Encoder()
        this.decoder = if (opts.decoder != null) opts.decoder else IOParser.Decoder()
    }

    private fun emitAll(event: String, vararg args: Any?) {
        this.emit(event, *args)
        for (socket in this.nsps.values) {
            socket.emit(event, args)
        }
    }

    /**
     * Update `socket.id` of all sockets
     */
    private fun updateSocketIds() {
        for (entry in this.nsps.entries) {
            val nsp = entry.key
            val socket = entry.value
            socket.id = this.generateId(nsp)
        }
    }

    private fun generateId(nsp: String): String {
        return (if ("/" == nsp) "" else "$nsp#") + this.engine.id()
    }

    fun reconnection(): Boolean {
        return this._reconnection
    }

    fun reconnection(v: Boolean): Manager {
        this._reconnection = v
        return this
    }

    fun reconnectionAttempts(): Int {
        return this._reconnectionAttempts
    }

    fun reconnectionAttempts(v: Int): Manager {
        this._reconnectionAttempts = v
        return this
    }

    fun reconnectionDelay(): Long {
        return this._reconnectionDelay
    }

    fun reconnectionDelay(v: Long): Manager {
        this._reconnectionDelay = v
        if (this.backoff != null) {
            this.backoff.setMin(v)
        }
        return this
    }

    fun randomizationFactor(): Double {
        return this._randomizationFactor
    }

    fun randomizationFactor(v: Double): Manager {
        this._randomizationFactor = v
        if (this.backoff != null) {
            this.backoff.setJitter(v)
        }
        return this
    }

    fun reconnectionDelayMax(): Long {
        return this._reconnectionDelayMax
    }

    fun reconnectionDelayMax(v: Long): Manager {
        this._reconnectionDelayMax = v
        this.backoff?.setMax(v)
        return this
    }

    fun timeout(): Long {
        return this._timeout
    }

    fun timeout(v: Long): Manager {
        this._timeout = v
        return this
    }

    private fun maybeReconnectOnOpen() {
        // Only try to reconnect if it's the first time we're connecting
        if (!this.reconnecting && this._reconnection && this.backoff?.attempts == 0) {
            this.reconnect()
        }
    }

    /**
     * Connects the client.
     *
     * @param fn callback.
     * @return a reference to this object.
     */
    @JvmOverloads
    fun open(fn: OpenCallback? = null): Manager {
        EventThread.exec(Runnable {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(String.format("readyState %s", this@Manager.readyState))
            }
            if (this@Manager.readyState == ReadyState.OPEN || this@Manager.readyState == ReadyState.OPENING) return@Runnable

            if (logger.isLoggable(Level.FINE)) {
                logger.fine(String.format("opening %s", this@Manager.uri))
            }
            this@Manager.engine = Engine(this@Manager.uri, this@Manager.opts)
            val socket = this@Manager.engine
            val self = this@Manager
            this@Manager.readyState = ReadyState.OPENING
            this@Manager.skipReconnect = false

            // propagate transport event.
            socket.on(
                EVENT_TRANSPORT
            ) { args -> self.emit(Manager.EVENT_TRANSPORT, *args) }

            val openSub = On.on(socket, EVENT_OPEN, Listener {
                self.onopen()
                fn?.call(null)
            })

            val errorSub = On.on(socket, EVENT_ERROR, Listener { objects ->
                val data = if (objects.isNotEmpty()) objects[0] else null
                logger.fine("connect_error")
                self.cleanup()
                self.readyState = ReadyState.CLOSED
                self.emitAll(EVENT_CONNECT_ERROR, data)
                if (fn != null) {
                    val err = SocketIOException(
                        "Connection error",
                        if (data is Exception) data else Throwable()
                    )
                    fn.call(err)
                } else {
                    // Only do this if there is no fn to handle the error
                    self.maybeReconnectOnOpen()
                }
            })

            if (this@Manager._timeout >= 0) {
                val timeout = this@Manager._timeout
                logger.fine(String.format("connection attempt will timeout after %d", timeout))

                val timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        EventThread.exec {
                            logger.fine(String.format("connect attempt timed out after %d", timeout))
                            openSub.destroy()
                            socket.close()
                            socket.emit(EVENT_ERROR, SocketIOException("timeout"))
                            self.emitAll(EVENT_CONNECT_TIMEOUT, timeout)
                        }
                    }
                }, timeout)

                this@Manager.subs.add(object : On.Handle {
                    override fun destroy() {
                        timer.cancel()
                    }
                })
            }

            this@Manager.subs.add(openSub)
            this@Manager.subs.add(errorSub)

            this@Manager.engine.open()
        })
        return this
    }

    private fun onopen() {
        logger.fine("open")

        this.cleanup()

        this.readyState = ReadyState.OPEN
        this.emit(EVENT_OPEN)

        val socket = this.engine
        this.subs.add(On.on(socket, EVENT_DATA, Listener { objects ->
            val data = objects[0]
            if (data is String) {
                this@Manager.ondata(data)
            } else if (data is ByteArray) {
                this@Manager.ondata(data)
            }
        }))
        this.subs.add(On.on(socket, EVENT_PING, Listener { this@Manager.onping() }))
        this.subs.add(On.on(socket, EVENT_PONG, Listener { this@Manager.onpong() }))
        this.subs.add(
            On.on(
                socket, EVENT_ERROR,
                Listener { objects -> this@Manager.onerror(objects[0] as Exception) })
        )
        this.subs.add(
            On.on(
                socket, EVENT_CLOSE,
                Listener { objects -> this@Manager.onclose(objects[0] as String) })
        )
        this.decoder!!.onDecoded(object : Parser.Decoder.Callback {
            override fun call(packet: Packet) {
                this@Manager.ondecoded(packet)
            }
        })
    }

    private fun onping() {
        this.lastPing = Date()
        this.emitAll(EVENT_PING)
    }

    private fun onpong() {
        this.emitAll(
            EVENT_PONG,
            if (null != this.lastPing) Date().time - this.lastPing!!.time else 0
        )
    }

    private fun ondata(data: String) {
        this.decoder!!.add(data)
    }

    private fun ondata(data: ByteArray) {
        this.decoder!!.add(data)
    }

    private fun ondecoded(packet: Packet) {
        this.emit(EVENT_PACKET, packet)
    }

    private fun onerror(err: Exception) {
        logger.log(Level.FINE, "error", err)
        this.emitAll(EVENT_ERROR, err)
    }

    /**
     * Initializes [Socket] instances for each namespaces.
     *
     * @param nsp namespace.
     * @param opts options.
     * @return a socket instance for the namespace.
     */
    fun socket(nsp: String, opts: Options? = null): Socket {
        var socket = this.nsps[nsp]
        if (socket == null) {
            socket = Socket(this, nsp, opts)
            val _socket = this.nsps.putIfAbsent(nsp, socket)
            if (_socket != null) {
                socket = _socket
            } else {
                val self = this
                val s = socket
                socket.on(Socket.EVENT_CONNECTING, Listener { self.connecting.add(s) })
                socket.on(Socket.EVENT_CONNECT, Listener { s.id = self.generateId(nsp) })
            }
        }
        return socket
    }

    /*package*/ internal fun destroy(socket: Socket) {
        this.connecting.remove(socket)
        if (!this.connecting.isEmpty()) return

        this.close()
    }

    /*package*/ internal fun packet(packet: Packet) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(String.format("writing packet %s", packet))
        }
        val self = this

        if (packet.query != null && !packet.query!!.isEmpty() && packet.type == CONNECT) {
            packet.nsp += "?" + packet.query
        }

        if (!self.encoding) {
            self.encoding = true
            this.encoder!!.encode(packet, object : Parser.Encoder.Callback {
                override fun call(encodedPackets: Array<Any>) {
                    for (packet in encodedPackets) {
                        if (packet is String) {
                            self.engine.write(packet)
                        } else if (packet is ByteArray) {
                            self.engine.write(packet)
                        }
                    }
                    self.encoding = false
                    self.processPacketQueue()
                }
            })
        } else {
            self.packetBuffer.add(packet)
        }
    }

    private fun processPacketQueue() {
        if (!this.packetBuffer.isEmpty() && !this.encoding) {
            val pack = this.packetBuffer.removeAt(0)
            this.packet(pack)
        }
    }

    private fun cleanup() {
        logger.fine("cleanup")

        var sub: On.Handle? = this.subs.poll()
        while (sub != null) {
            sub = this.subs.poll()
            sub.destroy()
        }

//        this.decoder?.onDecoded(null)

        this.packetBuffer.clear()
        this.encoding = false
        this.lastPing = null

        this.decoder?.destroy()
    }

    /*package*/ internal fun close() {
        logger.fine("disconnect")
        this.skipReconnect = true
        this.reconnecting = false
        if (this.readyState != ReadyState.OPEN) {
            // `onclose` will not fire because
            // an open event never happened
            this.cleanup()
        }
        this.backoff?.reset()
        this.readyState = ReadyState.CLOSED
        if (this.engine != null) {
            this.engine.close()
        }
    }

    private fun onclose(reason: String) {
        logger.fine("onclose")
        this.cleanup()
        this.backoff?.reset()
        this.readyState = ReadyState.CLOSED
        this.emit(EVENT_CLOSE, reason)

        if (this._reconnection && !this.skipReconnect) {
            this.reconnect()
        }
    }

    private fun reconnect() {
        if (this.reconnecting || this.skipReconnect) return

        val self = this

        backoff?.let {

            if (it.attempts >= this._reconnectionAttempts) {
                logger.fine("reconnect failed")
                it.reset()
                this.emitAll(EVENT_RECONNECT_FAILED)
                this.reconnecting = false
            } else {
                val delay = it.duration()
                logger.fine(String.format("will wait %dms before reconnect attempt", delay))

                this.reconnecting = true
                val timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        EventThread.exec(Runnable {
                            if (self.skipReconnect) return@Runnable

                            logger.fine("attempting reconnect")
                            val attempts = it.attempts
                            self.emitAll(EVENT_RECONNECT_ATTEMPT, attempts)
                            self.emitAll(EVENT_RECONNECTING, attempts)

                            // check again for the case socket closed in above events
                            if (self.skipReconnect) return@Runnable

                            self.open(object : OpenCallback {
                                override fun call(err: Exception?) {
                                    if (err != null) {
                                        logger.fine("reconnect attempt error")
                                        self.reconnecting = false
                                        self.reconnect()
                                        self.emitAll(EVENT_RECONNECT_ERROR, err)
                                    } else {
                                        logger.fine("reconnect success")
                                        self.onreconnect()
                                    }
                                }
                            })
                        })
                    }
                }, delay)

                this.subs.add(object : On.Handle {
                    override fun destroy() {
                        timer.cancel()
                    }
                })
            }

        }

    }

    private fun onreconnect() {
        val attempts = this.backoff?.attempts
        this.reconnecting = false
        this.backoff?.reset()
        this.updateSocketIds()
        this.emitAll(EVENT_RECONNECT, attempts)
    }


    companion object {
        interface OpenCallback {

            fun call(err: Exception?)
        }


        open class Engine internal constructor(uri: URI, opts: Options) :
            io.socket.engineio.client.Socket(uri, opts)

        open class Options : io.socket.engineio.client.Socket.Options() {

            var reconnection = true
            var reconnectionAttempts: Int = 0
            var reconnectionDelay: Long = 0
            var reconnectionDelayMax: Long = 0
            var randomizationFactor: Double = 0.toDouble()
            var encoder: Parser.Encoder? = null
            var decoder: Parser.Decoder? = null

            /**
             * Connection timeout (ms). Set -1 to disable.
             */
            var timeout: Long = 20000
        }


        private val logger = Logger.getLogger(Manager::class.java.name)

        /**
         * Called on a successful connection.
         */
        val EVENT_OPEN = "open"

        /**
         * Called on a disconnection.
         */
        val EVENT_CLOSE = "close"

        val EVENT_PACKET = "packet"
        val EVENT_ERROR = "error"

        /**
         * Called on a connection error.
         */
        val EVENT_CONNECT_ERROR = "connect_error"

        /**
         * Called on a connection timeout.
         */
        val EVENT_CONNECT_TIMEOUT = "connect_timeout"

        /**
         * Called on a successful reconnection.
         */
        val EVENT_RECONNECT = "reconnect"

        /**
         * Called on a reconnection attempt error.
         */
        val EVENT_RECONNECT_ERROR = "reconnect_error"

        val EVENT_RECONNECT_FAILED = "reconnect_failed"

        val EVENT_RECONNECT_ATTEMPT = "reconnect_attempt"

        val EVENT_RECONNECTING = "reconnecting"

        val EVENT_PING = "ping"

        val EVENT_PONG = "pong"

        /**
         * Called when a new transport is created. (experimental)
         */
        val EVENT_TRANSPORT = "transport"

        /*package*/ internal var defaultWebSocketFactory: WebSocket.Factory? = null
        /*package*/ internal var defaultCallFactory: Call.Factory? = null
    }
}
