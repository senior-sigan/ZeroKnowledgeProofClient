package org.seniorsigan.qrauthenticatorclient

data class CommonResponse(
    val success: Boolean = false,
    val error: String = "",
    val message: String = ""
)