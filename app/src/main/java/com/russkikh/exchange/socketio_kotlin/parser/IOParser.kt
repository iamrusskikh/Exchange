package com.russkikh.exchange.socketio_kotlin.parser

import android.util.Log
import com.russkikh.exchange.socketio_kotlin.extensions.*
import org.json.JSONException
import org.json.JSONTokener


private fun socketError(): Packet {
    return Packet(type = ERROR, data = "parser error")
}

class IOParser:Parser {


    class Encoder : Parser.Encoder {

        override fun encode(obj: Packet, callback: Parser.Encoder.Callback) {
            if ((obj.type == EVENT || obj.type == ACK) && isBinaryObject(obj.data)) {
                obj.type = if (obj.type == EVENT) BINARY_EVENT else BINARY_ACK
            }

            if (Log.isLoggable(TAG, Log.INFO)) {
                Log.i(TAG, String.format("encoding packet %s", obj))
            }

            if (BINARY_EVENT == obj.type || BINARY_ACK == obj.type) {
                encodeAsBinary(obj, callback)
            } else {
                val encoding = encodeAsString(obj)
                callback.call(arrayOf(encoding))
            }
        }

        private fun encodeAsString(obj: Packet): String {
            val str = StringBuilder("" + obj.type)

            if (BINARY_EVENT == obj.type || BINARY_ACK == obj.type) {
                str.append(obj.attachments)
                str.append("-")
            }

            if (obj.nsp.isNotEmpty() && "/" != obj.nsp) {
                str.append(obj.nsp)
                str.append(",")
            }

            if (obj.id >= 0) {
                str.append(obj.id)
            }

            if (obj.data != null) {
                str.append(obj.data)
            }

            if (Log.isLoggable(TAG, Log.INFO)) {
                Log.i(TAG, String.format("encoded %s as %s", obj, str))
            }

            return str.toString()
        }

        private fun encodeAsBinary(obj: Packet, callback: Parser.Encoder.Callback) {
            val deconstruction = Binary.deconstructPacket(obj)
            val pack = encodeAsString(deconstruction.packet)
            val buffers = ArrayList<Any>(listOf(deconstruction.buffers))

            buffers.add(0, pack)
            callback.call(buffers.toTypedArray())
        }
    }


    class Decoder : Parser.Decoder {
        override fun onDecoded(callback: Parser.Decoder.Callback) {
                this.onDecodedCallback = callback
        }

        private lateinit var reconstructor: BinaryReconstructor

        lateinit var onDecodedCallback: Parser.Decoder.Callback


        override fun add(obj: String) {
            val packet = decodeString(obj)
            if (BINARY_EVENT == packet.type || BINARY_ACK == packet.type) {
                this.reconstructor = BinaryReconstructor(packet)

                if (this.reconstructor.reconPack.attachments == 0) {
                    if (::onDecodedCallback.isInitialized) {
                        this.onDecodedCallback.call(packet)
                    }
                }
            } else {
                if (::onDecodedCallback.isInitialized) {
                    this.onDecodedCallback.call(packet)
                }
            }
        }

        override fun add(obj: ByteArray) {
            if (!::reconstructor.isInitialized) {
                throw RuntimeException("got binary data when not reconstructing a packet")
            } else {
                val packet = this.reconstructor.takeBinaryData(obj)
                if (packet != null) {
                    if (::onDecodedCallback.isInitialized) {
                        this.onDecodedCallback.call(packet)
                    }
                }
            }
        }

        private fun decodeString(str: String): Packet {
            var i = 0
            val length = str.length

            val p = Packet(Character.getNumericValue(str[0]))

            if (p.type < 0 || p.type > types.size - 1) return socketError()

            if (BINARY_EVENT == p.type || BINARY_ACK == p.type) {
                if (!str.contains("-") || length <= i + 1) return socketError()
                val attachments = StringBuilder()
                while (str[++i] != '-') {
                    attachments.append(str[i])
                }
                p.attachments = Integer.parseInt(attachments.toString())
            }

            if (length > i + 1 && '/' == str[i + 1]) {
                val nsp = StringBuilder()
                while (true) {
                    ++i
                    val c = str[i]
                    if (',' == c) break
                    nsp.append(c)
                    if (i + 1 == length) break
                }
                p.nsp = nsp.toString()
            } else {
                p.nsp = "/"
            }

            if (length > i + 1) {
                val next = str[i + 1]
                if (Character.getNumericValue(next) > -1) {
                    val id = StringBuilder()
                    while (true) {
                        ++i
                        val c = str[i]
                        if (Character.getNumericValue(c) < 0) {
                            --i
                            break
                        }
                        id.append(c)
                        if (i + 1 == length) break
                    }
                    try {
                        p.id = Integer.parseInt(id.toString())
                    } catch (e: NumberFormatException) {
                        return socketError()
                    }

                }
            }

            if (length > i + 1) {
                try {
                    str[++i]
                    p.data = JSONTokener(str.substring(i)).nextValue()
                } catch (e: JSONException) {
                    Log.w(TAG, "An error occured while retrieving data from JSONTokener", e)
                    return socketError()
                }

            }

            if (Log.isLoggable(TAG, Log.INFO)) {
                Log.i(TAG, String.format("decoded %s as %s", str, p))
            }
            return p
        }

        override fun destroy() {
            if (::reconstructor.isInitialized ) {
                this.reconstructor.finishReconstruction()
            }
        }


    }


    /*package*/ internal class BinaryReconstructor(var reconPack: Packet) {

        /*package*/  var buffers: MutableList<ByteArray>

        init {
            this.buffers = ArrayList()
        }

        fun takeBinaryData(binData: ByteArray): Packet? {
            this.buffers.add(binData)
            if (this.buffers.size == this.reconPack.attachments) {
                val packet = Binary.reconstructPacket(
                    this.reconPack,
                    this.buffers.toTypedArray()
                )
                this.finishReconstruction()
                return packet
            }
            return null
        }

        fun finishReconstruction() {
            this.buffers = ArrayList()
        }
    }

}