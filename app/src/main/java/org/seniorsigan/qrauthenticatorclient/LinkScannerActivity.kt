package org.seniorsigan.qrauthenticatorclient

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log

class LinkScannerActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Start Link Token Parsing")
        Log.d(TAG, "Get ${intent.data} from url")
        val data = intent.data.getQueryParameter("token")
        val token = String(Base64.decode(data, Base64.DEFAULT))

        val intent = Intent(this, TokenParserActivity::class.java)
        intent.putExtra(RAW_TOKEN_INTENT, token)
        startActivity(intent)

        Log.d(TAG, "LinkScannerActivity finished")
        finish()
    }
}