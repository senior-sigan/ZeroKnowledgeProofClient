package org.seniorsigan.qrauthenticatorclient

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.squareup.okhttp.*
import java.io.IOException

class SignupActivity : AppCompatActivity() {
    val keysAmount = 1000
    val jsonType = MediaType.parse("application/json; charset=utf-8")
    val protocol = "https://"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        val token = intent.getSerializableExtra(SIGNUP_TOKEN_INTENT) as Token
        val btn = findViewById(R.id.signUpBtn) as Button
        val login = findViewById(R.id.signUpLogin) as EditText
        btn.setOnClickListener {view ->
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
                            Log.d(TAG, "Failed request to ${token.domainName}${token.path}: ${e?.message}")
                        }

                        override fun onResponse(response: Response?) {
                            if (response != null) {
                                Log.d(TAG, response.body().string())
                            }
                        }
                    })

                }
            }
        }
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
