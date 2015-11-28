package org.seniorsigan.qrauthenticatorclient

import java.security.MessageDigest

class KeysGenerator {
    val algorythm: String = "SHA-256"

    fun generateSequence(nonce: String, times: Int): List<String> {
        val bNonce = nonce.toByteArray("UTF-8")
        val seq: MutableList<ByteArray> = arrayListOf()
        seq.add(generate(bNonce))
        for (i in 1..times) {
            seq.add(i, generate(seq.last()))
        }

        return seq.map { it.toHexString() }
    }

    fun generate(nonce: ByteArray): ByteArray {
        val md = MessageDigest.getInstance(algorythm)
        md.update(nonce)
        return md.digest()
    }

    fun generate(nonce: String): String {
        val bNonce = nonce.fromHex()
        val md = MessageDigest.getInstance(algorythm)
        md.update(bNonce)
        return md.digest().toHexString()
    }
}