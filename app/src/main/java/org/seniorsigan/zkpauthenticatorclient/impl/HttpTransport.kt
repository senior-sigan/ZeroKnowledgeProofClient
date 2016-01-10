package org.seniorsigan.zkpauthenticatorclient.impl

import com.squareup.okhttp.*
import org.seniorsigan.zkpauthenticator.Transport
import org.seniorsigan.zkpauthenticator.TransportCallback
import java.io.IOException

class HttpTransport(
        val client: OkHttpClient,
        val protocol: String = "https://"
): Transport {
    val jsonType = MediaType.parse("application/json; charset=utf-8")

    override fun send(data: String, address: String, callback: TransportCallback) {
        val url = protocol + address
        val body = RequestBody.create(jsonType, data)
        val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

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