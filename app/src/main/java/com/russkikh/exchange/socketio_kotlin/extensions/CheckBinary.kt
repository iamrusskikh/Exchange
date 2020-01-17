package com.russkikh.exchange.socketio_kotlin.extensions

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.logging.Level
import java.util.logging.Logger


fun Any.isBinaryObject(obj: Any?): Boolean {

    if (obj == null) return false

    if (obj is ByteArray) {
        return true
    }

    when (obj) {
        is JSONArray -> {
            val length = obj.length()
            for (i in 0 until length) {
                val v: Any?
                try {
                    v = if (obj.isNull(i)) null else obj.get(i)
                } catch (e: JSONException) {
                    Log.e(TAG,"Error in JSON Array", e)
                    return false
                }

                if (isBinaryObject(v)) {
                    return true
                }
            }
        }
        is JSONObject -> {
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next() as String
                val v: Any
                try {
                    v = obj.get(key)
                } catch (e: JSONException) {
                    Log.e(TAG,"Error in JSON Object", e)
                    return false
                }

                if (isBinaryObject(v)) {
                    return true
                }
            }
        }
    }

    return false
}


object HasBinary {

    private val logger = Logger.getLogger(HasBinary::class.java.name)

    fun hasBinary(data: Any): Boolean {
        return _hasBinary(data)
    }

    private fun _hasBinary(obj: Any?): Boolean {
        if (obj == null) return false

        if (obj is ByteArray) {
            return true
        }

        if (obj is JSONArray) {
            val _obj = obj as JSONArray?
            val length = _obj!!.length()
            for (i in 0 until length) {
                val v: Any?
                try {
                    v = if (_obj.isNull(i)) null else _obj.get(i)
                } catch (e: JSONException) {
                    logger.log(Level.WARNING, "An error occured while retrieving data from JSONArray", e)
                    return false
                }

                if (_hasBinary(v)) {
                    return true
                }
            }
        } else if (obj is JSONObject) {
            val _obj = obj as JSONObject?
            val keys = _obj!!.keys()
            while (keys.hasNext()) {
                val key = keys.next() as String
                val v: Any
                try {
                    v = _obj.get(key)
                } catch (e: JSONException) {
                    logger.log(Level.WARNING, "An error occured while retrieving data from JSONObject", e)
                    return false
                }

                if (_hasBinary(v)) {
                    return true
                }
            }
        }

        return false
    }
}
