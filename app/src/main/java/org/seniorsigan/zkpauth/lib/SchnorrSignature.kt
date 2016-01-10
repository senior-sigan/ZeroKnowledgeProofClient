package org.seniorsigan.zkpauth.lib

import java.io.Serializable
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.interfaces.DSAPrivateKey
import java.security.interfaces.DSAPublicKey
import kotlin.math.minus
import kotlin.math.times

/**
 * Schnorr signature authentication scheme.
 * @see(https://en.wikipedia.org/wiki/Schnorr_signature)
 */
class SchnorrSignature {
    /**
     * Bit size of prime number "p"
     */
    val keySize = 1024
    /**
     * Nonce bit size.
     */
    val t = 160

    val random = SecureRandom()

    data class SchnorrKeyPair(
            val public: SchnorrPublicKey,
            val private: SchnorrPrivateKey
    ): Serializable
    data class SchnorrPrivateKey(
            val w: BigInteger
    ): Serializable
    data class SchnorrPublicKey(
            val p: BigInteger,
            val q: BigInteger,
            val g: BigInteger,
            val y: BigInteger
    ): Serializable
    data class SessionPair(
            val x: BigInteger,
            val r: BigInteger
    ): Serializable

    fun generateKey(): SchnorrKeyPair {
        val keyGen = KeyPairGenerator.getInstance("DSA")
        keyGen.initialize(keySize, random)
        val pair = keyGen.generateKeyPair()
        val publicDSA = pair.public as DSAPublicKey
        val privateDSA = pair.private as DSAPrivateKey
        val params = privateDSA.params
        val public = SchnorrPublicKey(params.p, params.q, params.g, publicDSA.y)
        val private = SchnorrPrivateKey(privateDSA.x)
        return SchnorrKeyPair(public, private)
    }

    fun generateSessionPair(params: SchnorrPublicKey): SessionPair {
        var r: BigInteger
        do {
            r = BigInteger(params.q.bitLength(), random)
        } while (r < params.q)
        val x = params.g.modPow(r, params.p)
        return SessionPair(x, r)
    }

    fun calculateS(session: SessionPair, pair: SchnorrKeyPair, nonce: BigInteger): BigInteger {
        return (session.r - pair.private.w * nonce) % pair.public.q
    }

    fun verify(x: BigInteger, s: BigInteger, e: BigInteger, public: SchnorrPublicKey): Boolean {
        val res = public.g.modPow(s, public.p) * public.y.modPow(e, public.p) % public.p
        return x == res
    }

    fun generateNonce(): BigInteger {
        return BigInteger(t, random)
    }
}
