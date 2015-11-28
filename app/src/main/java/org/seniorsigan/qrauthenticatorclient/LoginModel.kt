package org.seniorsigan.qrauthenticatorclient

data class LoginModel(
    val login: String,
    val key: String,
    val token: String
)