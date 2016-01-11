package org.seniorsigan.zkpauthenticatorclient.impl

import android.util.Log
import com.squareup.okhttp.*
import org.seniorsigan.zkpauthenticator.Transport
import org.seniorsigan.zkpauthenticator.TransportCallback
import org.seniorsigan.zkpauthenticatorclient.TAG
import java.io.IOException

class HttpTransport(
        val client: OkHttpClient
): Transport {
    val jsonType = MediaType.parse("application/json; charset=utf-8")
    var protocol: String = "https://"

    override fun send(data: String, address: String, callback: TransportCallback) {
        if (address.contains("localhost") || address.contains("192.168.0.1")) {
            Log.w(TAG, "Use not secure http protocol")
            protocol = "http://"
        }
        val url = protocol + address
        val body = RequestBody.create(jsonType, data)
        val request = Request.Builder()
                .url(url)
                .post(body)
                .build()
        Log.d(TAG, "Send request to $url")
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(request: Request?, e: IOException?) {
                callback.onFailure(e ?: Exception("Fatal error in OkHttp"))
            }

            override fun onResponse(response: Response?) {
                if (response != null && response.isSuccessful) {
                    callback.onSuccess(response.body()?.string() ?: "")
                } else {
                    callback.onFailure(Exception("Server respond with error ${response?.code()} ${response?.body()?.string()}"))
                }
            }

        })
    }
}