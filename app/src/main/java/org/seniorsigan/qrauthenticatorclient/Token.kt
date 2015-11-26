package org.seniorsigan.qrauthenticatorclient

import java.util.*

data class Token(
        val domainName: String,
        val token: String,
        val path: String,
        val expiresAt: Date,
        val type: String
)