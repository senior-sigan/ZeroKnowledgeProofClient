package org.seniorsigan.zkpauthenticatorclient

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import org.seniorsigan.zkpauthenticator.Token

class TokenParserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Start Token Parsing")
        val tokenJson = intent.getStringExtra(RAW_TOKEN_INTENT)
        try {
            val token = App.gson.fromJson(tokenJson, Token::class.java)
            if (token != null) {
                Log.d(TAG, "Token parsed successful ${token.toString()}")
                when (token.type) {
                    "LOGIN" -> goToLogin(token)
                    "SIGNUP" -> goToSignup(token)
                    else -> goToError("Undefined token type '${token.type}'")
                }
            } else {
                goToError("Token is empty: $tokenJson")
            }
        } catch (e: Exception) {
            goToError("Json parse error for $tokenJson: ${e.message}")
        }
        Log.d(TAG, "TokenParserActivity finished")
        finish()
    }

    private fun goToError(error: String) {
        Log.e(TAG, error)
        val intent = Intent(this, FailureActivity::class.java)
        intent.putExtra(FAILURE_INTENT, error)
        startActivity(intent)
        finish()
    }

    private fun goToSignup(token: Token) {
        Log.d(TAG, "Go to signup")
        val intent = Intent(this, SignupActivity::class.java)
        intent.putExtra(SIGNUP_TOKEN_INTENT, token)
        startActivity(intent)
    }

    private fun goToLogin(token: Token) {
        Log.d(TAG, "Go to login")
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra(LOGIN_TOKEN_INTENT, token)
        startActivity(intent)
    }
}
