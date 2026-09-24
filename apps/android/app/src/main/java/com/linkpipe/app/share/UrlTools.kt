package com.linkpipe.app.share

import java.net.URI
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object UrlTools {
    private val urlPattern = Regex("https?://[^\\s<>\\\"]+", RegexOption.IGNORE_CASE)

    fun extractFirstUrl(text: String?): String? = text
        ?.let(urlPattern::find)
        ?.value
        ?.trimEnd('.', ',', ';', ':', '!', '?', ')', ']', '}')

    fun normalize(rawUrl: String): String {
        val uri = URI(rawUrl.trim())
        require(uri.scheme.equals("http", true) || uri.scheme.equals("https", true))
        require(!uri.host.isNullOrBlank())
        val scheme = uri.scheme.lowercase()
        val host = uri.host.lowercase()
        val port = uri.port.takeUnless { it == -1 || (scheme == "http" && it == 80) || (scheme == "https" && it == 443) }
            ?: -1
        val rawPath = (uri.rawPath ?: "").let { path ->
            if (path.length > 1 && path.endsWith('/')) path.dropLast(1) else path
        }
        val sortedQuery = uri.rawQuery
            ?.split('&')
            ?.filter(String::isNotEmpty)
            ?.sortedWith(compareBy({ it.substringBefore('=') }, { it.substringAfter('=', "") }))
            ?.joinToString("&")
        return URI(scheme, uri.userInfo, host, port, rawPath, sortedQuery, null).toASCIIString()
    }

    fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}
