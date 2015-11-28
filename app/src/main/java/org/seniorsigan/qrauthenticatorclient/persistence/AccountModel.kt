package org.seniorsigan.qrauthenticatorclient.persistence

data class AccountModel(
        var id: Long = 0,
        val name: String,
        val domain: String,
        val tokens: List<String>,
        val currentToken: Int = 0
)