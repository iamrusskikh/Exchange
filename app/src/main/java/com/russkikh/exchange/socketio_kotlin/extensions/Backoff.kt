package com.russkikh.exchange.socketio_kotlin.extensions

import java.math.BigDecimal
import java.math.BigInteger


class Backoff {

    private var ms: Long = 100
    private var max: Long = 10000
    private var factor = 2L
    private var jitter: Double = 0.toDouble()
    var attempts: Int = 0
        private set

    fun duration(): Long {
        var ms = BigInteger.valueOf(this.ms)
            .multiply(BigInteger.valueOf(this.factor).pow(this.attempts++))
        if (jitter != 0.0) {
            val rand = Math.random()
            val deviation = BigDecimal.valueOf(rand)
                .multiply(BigDecimal.valueOf(jitter))
                .multiply(BigDecimal(ms)).toBigInteger()
            ms = if (Math.floor(rand * 10).toInt() and 1 == 0) ms.subtract(deviation) else ms.add(deviation)
        }
        return ms.min(BigInteger.valueOf(this.max)).toLong()
    }

    fun reset() {
        this.attempts = 0
    }

    fun setMin(min: Long): Backoff {
        this.ms = min
        return this
    }

    fun setMax(max: Long): Backoff {
        this.max = max
        return this
    }

    fun setFactor(factor: Long): Backoff {
        this.factor = factor
        return this
    }

    fun setJitter(jitter: Double): Backoff {
        this.jitter = jitter
        return this
    }
}