package org.seniorsigan.zkpauthenticatorclient.impl.skey

data class SKeySecret(
        val keys: List<String> = emptyList(),
        val currentKey: Int = 0
) {
    fun current(): String? {
        if (currentKey >= keys.size) return null
        return keys[currentKey]
    }
}