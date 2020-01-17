package com.russkikh.exchange.socketio_kotlin.extensions

const val TAG = "SOCKET-IO"


/**
 * Packet type `connect`.
 */
const val CONNECT = 0


/**
 * Packet type `disconnect`.
 */
const val DISCONNECT = 1

/**
 * Packet type `event`.
 */
const val EVENT = 2

/**
 * Packet type `ack`.
 */
const val ACK = 3

/**
 * Packet type `error`.
 */
const val ERROR = 4

/**
 * Packet type `binary event`.
 */
const val BINARY_EVENT = 5

/**
 * Packet type `binary ack`.
 */
const val BINARY_ACK = 6

const val PROTOCOL = 4

/**
 * Packet types.
 */
val types = arrayOf("CONNECT", "DISCONNECT", "EVENT", "ACK", "ERROR", "BINARY_EVENT", "BINARY_ACK")
