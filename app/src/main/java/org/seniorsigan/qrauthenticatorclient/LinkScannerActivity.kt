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
        Log.d(TAG, "Start parsing token $data")
        try {
            val token = String(Base64.decode(data, Base64.DEFAULT))
            val intent = Intent(this, TokenParserActivity::class.java)
            intent.putExtra(RAW_TOKEN_INTENT, token)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Can't decode base64 $data : ${e.message}")
            val intent = Intent(this, FailureActivity::class.java)
            intent.putExtra(FAILURE_INTENT, "Can't decode base64 $data")
            startActivity(intent)
        }

        Log.d(TAG, "LinkScannerActivity finished")
        finish()
    }
}