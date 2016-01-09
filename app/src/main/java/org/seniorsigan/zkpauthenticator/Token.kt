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
        val type: String = "" // login or signup
): Serializable {
    fun address(): String = domainName + path
}