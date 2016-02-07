package org.seniorsigan.zkpauthenticatorclient

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import org.jetbrains.anko.find
import org.jetbrains.anko.onClick
import org.jetbrains.anko.toast
import org.seniorsigan.zkpauthenticator.Authenticator
import org.seniorsigan.zkpauthenticator.Token

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        val token = intent.getSerializableExtra(SIGNUP_TOKEN_INTENT) as Token
        val loginInfo = find<TextView>(R.id.signupRequestInfo)
        loginInfo.text = "Signup request from ${token.requestInfo.ip}"
        val btn = find<Button>(R.id.signUpBtn)
        val login = find<EditText>(R.id.username)
        btn.onClick { view ->
            if (login.text != null && login.text.isNotBlank()) {
                signUp(login.text.toString(), token)
            }
        }
    }

    private fun signUp(name: String, token: Token) {
        try {
            val authenticator = App.authenticatorBuilder.get(token.algorithm)
            withPermission {
                authenticator.signup(token, name, object : Authenticator.AuthenticatorCallback {
                    override fun onSuccess(message: String) {
                        goToSuccess(message)
                    }

                    override fun onFailure(message: String, throwable: Throwable?) {
                        goToFailure(token, "$message: ${throwable?.message}")
                    }
                })
            }
        } catch (e: Exception) {
            goToFailure(token, e.message ?: "Unsupported algorithm ${token.algorithm}")
        }
    }
    private fun goToSuccess(message: String) {
        Log.d(TAG, "Go to success activity")
        val intent = Intent(this, SuccessActivity::class.java)
        intent.putExtra(SUCCESS_INTENT, message)
        startActivity(intent)
        finish()
    }

    private fun goToFailure(token: Token, error: String) {
        Log.e(TAG, "Can't signup with $token: $error")
        val intent = Intent(this, FailureActivity::class.java)
        intent.putExtra(FAILURE_INTENT, error)
        startActivity(intent)
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            App.CAN_USE_INTERNET -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permissions on Internet obtained")
                }
                else {
                    toast("You have not permission to use internet!")
                }
            }
        }
    }

    fun withPermission(block: () -> Unit) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), App.CAN_USE_INTERNET)
        } else {
            Log.d(TAG, "Permissions on Internet obtained")
            block()
        }
    }
}
