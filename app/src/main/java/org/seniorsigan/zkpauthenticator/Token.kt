package org.seniorsigan.zkpauthenticator

import java.io.Serializable
import java.util.*

data class Token(
        val domainName: String = "",
        val token: String = "", // short-living token for associating sessions
        val path: String = "",
        val expiresAt: Date = Date(),
        val payload: String = "", // some extra data from server
        val algorithm: String = "",
        val type: String = "", // login or signup
        val requestInfo: RequestInfo = RequestInfo()
): Serializable {
    fun address(): String = domainName + path
}

data class RequestInfo(
        val ip: String? = "",
        val host: String = "",
        val port: Int = -1,
        val userAgent: String = ""
): Serializable