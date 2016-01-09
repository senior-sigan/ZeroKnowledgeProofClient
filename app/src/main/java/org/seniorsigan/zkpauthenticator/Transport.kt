package org.seniorsigan.zkpauthenticator

interface Transport {
    fun send(data: String, address: String, callback: TransportCallback)
}

interface TransportCallback {
    fun onSuccess(data: String)
    fun onFailure(throwable: Throwable)
}