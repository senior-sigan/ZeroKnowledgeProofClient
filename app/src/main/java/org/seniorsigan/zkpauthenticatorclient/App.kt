package org.seniorsigan.zkpauthenticatorclient

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import android.util.Log
import com.google.gson.GsonBuilder
import com.squareup.okhttp.OkHttpClient
import org.seniorsigan.zkpauth.lib.SchnorrSignature
import org.seniorsigan.zkpauthenticator.AuthenticatorBuilder
import org.seniorsigan.zkpauthenticatorclient.impl.HttpTransport
import org.seniorsigan.zkpauthenticatorclient.impl.ObjectConverter
import org.seniorsigan.zkpauthenticatorclient.impl.repository.DatabaseOpenHelper
import org.seniorsigan.zkpauthenticatorclient.impl.repository.UserSQLRepository
import org.seniorsigan.zkpauthenticatorclient.impl.schnorr.SchnorrAuthenticator
import org.seniorsigan.zkpauthenticatorclient.impl.skey.SKeyAuthenticator
import java.security.SecureRandom
import java.util.*

const val TAG = "ZKPAuth"
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

        lateinit var userRepository: UserSQLRepository
        val transport = HttpTransport(httpClient, "http://")
        val converter = ObjectConverter(gson)
        val authenticatorBuilder = AuthenticatorBuilder()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "DB, Repository, Authenticators initialization")
        val dbHelper = DatabaseOpenHelper.getInstance(applicationContext)
        userRepository = UserSQLRepository(dbHelper)
        authenticatorBuilder.register(SKeyAuthenticator(
                userRepository,
                transport,
                keysGenerator,
                converter,
                secureRandom
        )).register(SchnorrAuthenticator(
                userRepository,
                transport,
                converter,
                secureRandom,
                SchnorrSignature()
        ))
    }
}