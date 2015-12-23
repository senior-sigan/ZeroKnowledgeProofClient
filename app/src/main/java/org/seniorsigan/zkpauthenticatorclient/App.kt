package org.seniorsigan.zkpauthenticatorclient

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import com.google.gson.GsonBuilder
import com.squareup.okhttp.OkHttpClient
import java.security.SecureRandom
import java.util.*

const val TAG = "QRAuth"
const val RAW_TOKEN_INTENT = "RAW_TOKEN_INTENT"
const val SIGNUP_TOKEN_INTENT = "SIGNUP_TOKEN_INTENT"
const val LOGIN_TOKEN_INTENT= "LOGIN_TOKEN_INTENT"
const val SUCCESS_INTENT = "SUCCESS_INTENT"
const val FAILURE_INTENT = "FAILURE_INTENT"

class App: Application() {
    companion object {
        val CAN_USE_CAMERA = 0x2
        val CAN_USE_INTERNET = 0x3
        val gsonBuilder = GsonBuilder().registerTypeAdapter(Date::class.java, DateDeserializer())
        val gson = gsonBuilder.create()
        val httpClient = OkHttpClient()
        val keysGenerator = KeysGenerator()
        val secureRandom = SecureRandom()
        fun <T> parseJson(rawJson: String, classOf: Class<T>): T? {
            try {
                return App.gson.fromJson(rawJson, classOf)
            } catch (e: Exception) {
                return null
            }
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}