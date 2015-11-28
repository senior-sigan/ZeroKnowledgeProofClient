package org.seniorsigan.qrauthenticatorclient

import java.io.Serializable
import java.util.*

data class Token(
        val domainName: String,
        val token: String,
        val path: String,
        val expiresAt: Date,
        val type: String
): Serializable