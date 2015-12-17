package org.seniorsigan.qrauthenticatorclient

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.squareup.okhttp.*
import org.jetbrains.anko.find
import org.jetbrains.anko.onUiThread
import org.jetbrains.anko.toast
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

        val accountsView = find<RecyclerView>(R.id.accounts_recycler_view)
        accountsView.setHasFixedSize(true)
        accountsView.layoutManager = LinearLayoutManager(this)
        val adapter = AccountsAdapter(accounts)
        adapter.onItemClickListener = {
            Log.d(TAG, "Selected account $it")
            sendLoginRequest(it, token)
        }
        accountsView.adapter = adapter
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
                            goToSuccess(account)
                        } else {
                            Log.d(TAG, "Server response with error $data on $token for $account")
                            goToFailure(account, token, "Server ${token.domainName} response with error $data on for ${account.name}")
                        }
                    }
                }

                override fun onFailure(request: Request?, e: IOException?) {
                    Log.d(TAG, "Failed request to ${token.domainName}${token.path} for $account because: ${e?.message}")
                    goToFailure(account, token, e?.message ?: "Error")
                }
            })
        }
    }

    private fun goToSuccess(account: AccountModel) {
        Log.d(TAG, "Go to success activity")
        val intent = Intent(this, SuccessActivity::class.java)
        intent.putExtra(SUCCESS_INTENT, account)
        startActivity(intent)
    }

    private fun goToFailure(account: AccountModel, token: Token, error: String) {
        val intent = Intent(this, FailureActivity::class.java)
        intent.putExtra(FAILURE_INTENT, error)
        startActivity(intent)
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
