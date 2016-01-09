package org.seniorsigan.zkpauthenticator

interface UserModel {
    val name: String
    val domain: String
    val algorithm: String
    var secret: String
}