package org.seniorsigan.qrauthenticatorclient

import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast

class TokenParserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Start Token Parsing")
        val tokenJson = intent.getStringExtra(RAW_TOKEN_INTENT)
        val token = App.gson.fromJson(tokenJson, Token::class.java)
        if (token != null) {
            Log.d(TAG, "Token parsed successful ${token.toString()}")
            when (token.type) {
                "LOGIN" -> goToLogin(token)
                "SIGNUP" -> goToSignup(token)
            }
        } else {
            goToError()
        }
    }

    private fun goToError() {

    }

    private fun goToSignup(token: Token) {
        Log.d(TAG, "Go to signup")
        val intent = Intent(this, SignupActivity::class.java)
        intent.putExtra(SIGNUP_TOKEN_INTENT, token)
        startActivity(intent)
    }

    private fun goToLogin(token: Token) {

    }
}
