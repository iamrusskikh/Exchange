package com.russkikh.exchange.socketio_kotlin.client


import okhttp3.Call
import okhttp3.WebSocket
import java.net.URI
import java.net.URISyntaxException
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger
import android.system.Os.socket
import android.util.Log
import com.russkikh.exchange.socketio_kotlin.parser.Parser
import com.russkikh.exchange.socketio_kotlin.extensions.PROTOCOL
import com.russkikh.exchange.socketio_kotlin.extensions.TAG
import com.russkikh.exchange.socketio_kotlin.extensions.Url
import java.util.logging.Level.FINE



class IO{

    companion object {


        private val managers = ConcurrentHashMap<String, Manager>()

        /**
         * Protocol version.
         */
        var protocol = PROTOCOL

        fun setDefaultOkHttpWebSocketFactory(factory: WebSocket.Factory) {
            Manager.defaultWebSocketFactory = factory
        }

        fun setDefaultOkHttpCallFactory(factory: Call.Factory) {
            Manager.defaultCallFactory = factory
        }


        @Throws(URISyntaxException::class)
        fun socket(uri: String): Socket {
            return socket(uri, null)
        }

        @Throws(URISyntaxException::class)
        fun socket(uri: String, opts: Options?): Socket {
            return socket(URI(uri), opts)
        }

        fun socket(uri: URI): Socket {
            return socket(uri, null)
        }

        /**
         * Initializes a [Socket] from an existing [Manager] for multiplexing.
         *
         * @param uri uri to connect.
         * @param opts options for socket.
         * @return [Socket] instance.
         */
        fun socket(uri: URI, opts: Options?): com.russkikh.exchange.socketio_kotlin.client.Socket {
            var opts = opts
            if (opts == null) {
                opts = Options()
            }

            val parsed = Url.parse(uri)
            val source: URI
            try {
                source = parsed.toURI()
            } catch (e: URISyntaxException) {
                throw RuntimeException(e)
            }

            val id = Url.extractId(parsed)
            val path = parsed.path
            val sameNamespace = managers.containsKey(id) && managers[id]!!.nsps.containsKey(path)
            val newConnection = opts.forceNew || !opts.multiplex || sameNamespace
            val io: Manager

            if (newConnection) {
                if (Log.isLoggable(TAG, Log.INFO)) {
                    Log.i(TAG, String.format("ignoring socket cache for %s", source))
                }
                io = Manager(source, opts)
            } else {
                if (!managers.containsKey(id)) {
                    if (Log.isLoggable(TAG, Log.INFO)) {
                        Log.i(TAG, String.format("new io instance for %s", source))
                    }
                    managers.putIfAbsent(id, Manager(source, opts))
                }
                io = managers[id]!!
            }

            val query = parsed.query
            if (query != null && (opts.query == null || opts.query.isEmpty())) {
                opts.query = query
            }

            return io.socket(parsed.path, opts)
        }

        class Options : Manager.Companion.Options() {

            var forceNew: Boolean = false

            /**
             * Whether to enable multiplexing. Default is true.
             */
            var multiplex = true
        }
    }

}