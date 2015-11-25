package org.seniorsigan.qrauthenticatorclient

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex

const val TAG = "QRAuth"

class App: Application() {
    companion object {
        val CAN_USE_CAMERA = 0x2
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}