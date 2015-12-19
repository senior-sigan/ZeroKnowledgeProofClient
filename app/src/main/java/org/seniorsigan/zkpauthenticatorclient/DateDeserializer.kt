package org.seniorsigan.zkpauthenticatorclient

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type
import java.util.*

class DateDeserializer: JsonDeserializer<Date> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Date {
        val timestamp = json?.asJsonPrimitive?.asLong
        if (timestamp != null) {
            return Date(timestamp)
        } else {
            throw JsonParseException("Can't parse date ${json?.asString}")
        }
    }
}