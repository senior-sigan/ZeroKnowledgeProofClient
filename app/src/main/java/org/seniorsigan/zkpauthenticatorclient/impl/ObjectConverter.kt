package org.seniorsigan.zkpauthenticatorclient.impl

import android.util.Log
import com.google.gson.Gson
import org.seniorsigan.zkpauthenticatorclient.TAG

class ObjectConverter(val gson: Gson) {
    fun to(o: Any): String {
        return gson.toJson(o)
    }

    fun <T> from(raw: String, clazz: Class<T>): T? {
        try {
            return gson.fromJson(raw, clazz)
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            return null
        }
    }
}