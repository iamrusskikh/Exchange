package com.russkikh.exchange.socketio_kotlin.parser

import android.util.Log
import com.russkikh.exchange.socketio_kotlin.extensions.TAG
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class Binary {

 companion object {
     private val KEY_PLACEHOLDER = "_placeholder"

     private val KEY_NUM = "num"

     fun deconstructPacket(packet: Packet): DeconstructedPacket {
         val buffers = ArrayList<ByteArray>()

         packet.data = _deconstructPacket(packet.data, buffers)
         packet.attachments = buffers.size

         return DeconstructedPacket(packet, buffers.toTypedArray())
     }

     private fun _deconstructPacket(data: Any?, buffers: MutableList<ByteArray>): Any? {
         if (data == null) return null

         when (data) {
             is ByteArray -> {
                 val placeholder = JSONObject()
                 try {
                     placeholder.put(KEY_PLACEHOLDER, true)
                     placeholder.put(KEY_NUM, buffers.size)
                 } catch (e: JSONException) {
                     Log.e(TAG,"Error in adding data to JSONObject", e)
                     return null
                 }

                 buffers.add(data)
                 return placeholder
             }
             is JSONArray -> {
                 val newData = JSONArray()
                 val len = data.length()
                 for (i in 0 until len) {
                     try {
                         newData.put(i, _deconstructPacket(data.get(i), buffers))
                     } catch (e: JSONException) {
                         Log.e(TAG,"Error in adding data to JSONObject", e)
                         return null
                     }

                 }
                 return newData
             }
             is JSONObject -> {
                 val newData = JSONObject()
                 val iterator = data.keys()
                 while (iterator.hasNext()) {
                     val key = iterator.next() as String
                     try {
                         newData.put(key, _deconstructPacket(data.get(key), buffers))
                     } catch (e: JSONException) {
                         Log.e(TAG,"Error in adding data to JSONObject", e)
                         return null
                     }

                 }
                 return newData
             }
             else -> return data
         }
     }

     fun reconstructPacket(packet: Packet, buffers: Array<ByteArray>): Packet {
         packet.data = _reconstructPacket(packet.data, buffers)
         packet.attachments = -1
         return packet
     }

     private fun _reconstructPacket(data: Any?, buffers: Array<ByteArray>): Any? {
         when (data) {
             is JSONArray -> {
                 val len = data.length()
                 for (i in 0 until len) {
                     try {
                         data.put(i, _reconstructPacket(data.get(i), buffers))
                     } catch (e: JSONException) {
                         Log.e(TAG,"Error in adding data to JSONObject", e)
                         return null
                     }

                 }
                 return data
             }
             is JSONObject -> {
                 if (data.optBoolean(KEY_PLACEHOLDER)) {
                     val num = data.optInt(KEY_NUM, -1)
                     return if (num >= 0 && num < buffers.size) buffers[num] else null
                 }
                 val iterator = data.keys()
                 while (iterator.hasNext()) {
                     val key = iterator.next() as String
                     try {
                         data.put(key, _reconstructPacket(data.get(key), buffers))
                     } catch (e: JSONException) {
                         Log.e(TAG,"Error in adding data to JSONObject", e)
                         return null
                     }

                 }
                 return data
             }
             else -> return data
         }
     }

     data class DeconstructedPacket(val packet: Packet, val buffers: Array<ByteArray>) {
         override fun equals(other: Any?): Boolean {
             if (this === other) return true
             if (javaClass != other?.javaClass) return false

             other as DeconstructedPacket

             if (packet != other.packet) return false
             if (!buffers.contentDeepEquals(other.buffers)) return false

             return true
         }

         override fun hashCode(): Int {
             var result = packet.hashCode()
             result = 31 * result + buffers.contentDeepHashCode()
             return result
         }
     }
 }

}