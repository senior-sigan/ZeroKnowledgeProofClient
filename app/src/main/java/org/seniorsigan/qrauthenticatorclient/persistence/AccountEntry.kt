package org.seniorsigan.qrauthenticatorclient.persistence

import android.provider.BaseColumns

object AccountEntry {
    val _ID = "id"
    val NAME = "name"
    val DOMAIN = "domain"
    val TOKENS = "tokens"
    val CURRENT_TOKEN = "current_token"
    val CREATED_AT = "created_at"
    val UPDATED_AT = "updated_at"
}