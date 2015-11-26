package org.seniorsigan.qrauthenticatorclient

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import com.google.gson.*
import java.lang.reflect.Type
import java.util.*

class AuthActivity : AppCompatActivity() {
    val gsonBuilder = GsonBuilder().registerTypeAdapter(Date::class.java, DateDeserializer())
    val gson = gsonBuilder.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show() }

        val tokenJson = intent.getStringExtra(RAW_TOKEN_INTENT)
        val token = gson.fromJson(tokenJson, Token::class.java)
        Log.d(TAG, token.toString())
    }

}
