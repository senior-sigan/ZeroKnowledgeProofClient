package org.seniorsigan.zkpauthenticatorclient

data class LoginModel(
    val login: String,
    val key: String,
    val token: String
)