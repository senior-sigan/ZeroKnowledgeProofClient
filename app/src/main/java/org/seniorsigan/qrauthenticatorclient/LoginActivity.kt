package org.seniorsigan.qrauthenticatorclient

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.squareup.okhttp.*
import org.seniorsigan.qrauthenticatorclient.persistence.AccountModel
import org.seniorsigan.qrauthenticatorclient.persistence.AccountsOpenHelper
import java.io.IOException

class LoginActivity : AppCompatActivity() {
    val protocol = "https://"
    val jsonType = MediaType.parse("application/json; charset=utf-8")
    lateinit var accountsDb: AccountsOpenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val token = intent.getSerializableExtra(LOGIN_TOKEN_INTENT) as Token
        accountsDb = AccountsOpenHelper(this)
        val accounts = accountsDb.findAccounts(token.domainName)
        Log.d(TAG, accounts.toString())
        sendLoginRequest(accounts.last(), token)
    }
    
    fun sendLoginRequest(account: AccountModel, token: Token) {
        val url = protocol + token.domainName + token.path
        val model = LoginModel(account.name, account.tokens[account.currentToken], token.token)
        val body = RequestBody.create(jsonType, App.gson.toJson(model))
        Log.d(TAG, "Sending to $url data $model")
        val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), App.CAN_USE_INTERNET)
        } else {
            Log.d(TAG, "Permissions on Internet obtained")
            App.httpClient.newCall(request).enqueue(object: Callback {
                override fun onResponse(response: Response?) {
                    if (response != null) {
                        val rawJson = response.body().string()
                        Log.d(TAG, "Get from $url $rawJson")
                        val data = App.gson.fromJson(rawJson, CommonResponse::class.java)
                        if (data != null && data.success) {
                            Log.d(TAG, "Logged in $url as ${account.name}")
                            accountsDb.nextTokenCount(account)
                        }
                    }
                }

                override fun onFailure(request: Request?, e: IOException?) {
                    Log.d(TAG, "Failed request to ${token.domainName}${token.path}: ${e?.message}")
                }

            })
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?) {
        when(requestCode) {
            App.CAN_USE_INTERNET -> {
                if (grantResults != null && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permissions on Internet obtained")
                }
                else {
                    Toast.makeText(
                            applicationContext,
                            "You have not permission to use internet!",
                            Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
