package org.seniorsigan.zkpauthenticatorclient.persistence

import java.io.Serializable
import java.util.*

data class AccountModel(
        var id: Long = 0,
        val name: String,
        val domain: String,
        val tokens: List<String>,
        val currentToken: Int = 0,
        val updatedAt: Date = Date(),
        val createdAt: Date = Date()
): Serializable