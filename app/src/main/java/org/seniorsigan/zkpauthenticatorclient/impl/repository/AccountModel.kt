package org.seniorsigan.zkpauthenticatorclient.impl.repository

import org.seniorsigan.zkpauthenticator.UserModel
import java.io.Serializable
import java.util.*

data class AccountModel(
        val _id: Long,
        override val name: String,
        override val domain: String,
        override var secret: String,
        override val algorithm: String,
        val createdAt: Date = Date(),
        val updatedAt: Date = Date()
): UserModel, Serializable