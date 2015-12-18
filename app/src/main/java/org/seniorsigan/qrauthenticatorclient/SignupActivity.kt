package org.seniorsigan.qrauthenticatorclient

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
import com.squareup.okhttp.*
import org.jetbrains.anko.find
import org.jetbrains.anko.onClick
import org.jetbrains.anko.onUiThread
import org.jetbrains.anko.toast
import org.seniorsigan.qrauthenticatorclient.persistence.AccountModel
import org.seniorsigan.qrauthenticatorclient.persistence.AccountsOpenHelper
import java.io.IOException

class SignupActivity : AppCompatActivity() {
    val keysAmount = 1000
    val jsonType = MediaType.parse("application/json; charset=utf-8")
    val protocol = "https://"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        val accountsDb = AccountsOpenHelper(this)
        val token = intent.getSerializableExtra(SIGNUP_TOKEN_INTENT) as Token
        val btn = find<Button>(R.id.signUpBtn)
        val login = find<EditText>(R.id.signUpLogin)
        btn.onClick { view ->
            if (login.text != null && login.text.isNotBlank()) {
                Log.d(TAG, "Start generating keys sequence for ${login.text.toString()}")
                val keys = App.keysGenerator.generateSequence(nonce(), keysAmount)
                Log.d(TAG, keys.toString())
                val model = SignupModel(login.text.toString(), keys.last(), token.token)
                val body = RequestBody.create(jsonType, App.gson.toJson(model))
                val url = protocol + token.domainName + token.path
                Log.d(TAG, "Sending to $url data $body")
                val request = Request.Builder()
                        .url(url)
                        .post(body)
                        .build()

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), App.CAN_USE_INTERNET)
                } else {
                    Log.d(TAG, "Permissions on Internet obtained")

                    App.httpClient.newCall(request).enqueue(object: Callback {
                        override fun onFailure(request: Request?, e: IOException?) {
                            goToFailure(token, "Failed request to ${token.domainName}${token.path}: ${e?.message}")
                        }

                        override fun onResponse(response: Response?) {
                            if (response != null) {
                                val rawJson = response.body().string()
                                Log.d(TAG, "Get from $url $rawJson")
                                val data = App.gson.fromJson(rawJson, CommonResponse::class.java)
                                if (data != null && data.success) {
                                    val account = AccountModel(
                                            name = model.login,
                                            domain = token.domainName,
                                            tokens = keys,
                                            currentToken = keys.size - 2
                                    )
                                    accountsDb.saveAccount(account)
                                    goToSuccess(account)
                                } else {
                                    goToFailure(token, "Error while signing up: $data")
                                }
                            }
                        }
                    })

                }
            }
        }
    }

    private fun goToSuccess(account: AccountModel) {
        Log.d(TAG, "Go to success activity")
        val intent = Intent(this, SuccessActivity::class.java)
        val message = "Signed up in ${account.domain} as ${account.name}. Tokens created ${account.tokens.size}"
        intent.putExtra(SUCCESS_INTENT, message)
        startActivity(intent)
        finish()
    }

    private fun goToFailure(token: Token, error: String) {
        Log.e(TAG, error)
        val intent = Intent(this, FailureActivity::class.java)
        intent.putExtra(FAILURE_INTENT, error)
        startActivity(intent)
        finish()
    }

    //TODO: may be nonce should include some value from server?
    fun nonce(): String {
        val bytes = ByteArray(128)
        App.secureRandom.nextBytes(bytes)
        return bytes.toHexString()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?) {
        when(requestCode) {
            App.CAN_USE_INTERNET -> {
                if (grantResults != null && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permissions on Internet obtained")
                }
                else {
                    toast("You have not permission to use internet!")
                }
            }
        }
    }
}
