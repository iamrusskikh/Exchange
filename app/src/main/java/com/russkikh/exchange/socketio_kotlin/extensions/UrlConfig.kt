package com.russkikh.exchange.socketio_kotlin.extensions


import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.util.regex.Pattern

object Url {

    private val PATTERN_HTTP = Pattern.compile("^http|ws$")
    private val PATTERN_HTTPS = Pattern.compile("^(http|ws)s$")

    @Throws(URISyntaxException::class)
    fun parse(uri: String): URL {
        return parse(URI(uri))
    }

    fun parse(uri: URI): URL {
        var protocol: String? = uri.scheme
        if (protocol == null || !protocol.matches("^https?|wss?$".toRegex())) {
            protocol = "https"
        }

        var port = uri.port
        if (port == -1) {
            if (PATTERN_HTTP.matcher(protocol).matches()) {
                port = 80
            } else if (PATTERN_HTTPS.matcher(protocol).matches()) {
                port = 443
            }
        }

        var path: String? = uri.rawPath
        if (path == null || path.isEmpty()) {
            path = "/"
        }

        val userInfo = uri.rawUserInfo
        val query = uri.rawQuery
        val fragment = uri.rawFragment
        try {
            return URL(
                "$protocol://${if (userInfo != null) "$userInfo@" else ""}${uri.host}${if (port != -1) ":$port" else ""}$path${if (query != null) "?$query" else ""}${if (fragment != null) "#$fragment" else ""}"
            )
        } catch (e: MalformedURLException) {
            throw RuntimeException(e)
        }

    }

    @Throws(MalformedURLException::class)
    fun extractId(url: String): String {
        return extractId(URL(url))
    }

    fun extractId(url: URL): String {
        val protocol = url.protocol
        var port = url.port
        if (port == -1) {
            if (PATTERN_HTTP.matcher(protocol).matches()) {
                port = 80
            } else if (PATTERN_HTTPS.matcher(protocol).matches()) {
                port = 443
            }
        }
        return "$protocol://${url.host}:$port"
    }

}
