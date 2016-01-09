package org.seniorsigan.zkpauthenticatorclient

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
import android.view.View
import android.widget.TextView
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import org.seniorsigan.zkpauthenticator.Authenticator
import org.seniorsigan.zkpauthenticator.Token
import org.seniorsigan.zkpauthenticatorclient.impl.repository.AccountModel

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val token = intent.getSerializableExtra(LOGIN_TOKEN_INTENT) as Token
        val accounts = App.userRepository.findByDomain(token.domainName, token.algorithm)
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

        val noAccountsView = find<TextView>(R.id.noAccountsView)
        noAccountsView.text = "You have no accounts for '${token.domainName}' :("
        if (accounts.isEmpty()) {
            noAccountsView.visibility = View.VISIBLE
        }
    }

    fun sendLoginRequest(account: AccountModel, token: Token) {
        try {
            val authenticator = App.authenticatorBuilder.get(token.algorithm)
            withPermission {
                authenticator.login(token, account.name, object : Authenticator.AuthenticatorCallback {
                    override fun onSuccess(message: String) {
                        goToSuccess(account)
                    }

                    override fun onFailure(message: String, throwable: Throwable?) {
                        Log.e(TAG, throwable?.message, throwable)
                        goToFailure(account, token, message)
                    }
                })
            }
        } catch (e: Exception) {
            goToFailure(account, token, e.message ?: "")
        }
    }

    private fun goToSuccess(account: AccountModel) {
        Log.d(TAG, "Go to success activity")
        val intent = Intent(this, SuccessActivity::class.java)
        val message = "Logged in ${account.domain} as ${account.name} with ${account.algorithm}."
        intent.putExtra(SUCCESS_INTENT, message)
        startActivity(intent)
        finish()
    }

    private fun goToFailure(account: AccountModel, token: Token, error: String) {
        Log.e(TAG, "Can't login user ${account.name}@${account.domain} with token $token: $error")
        val intent = Intent(this, FailureActivity::class.java)
        intent.putExtra(FAILURE_INTENT, error)
        startActivity(intent)
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?) {
        when (requestCode) {
            App.CAN_USE_INTERNET -> {
                if (grantResults != null && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permissions on Internet obtained")
                } else {
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
