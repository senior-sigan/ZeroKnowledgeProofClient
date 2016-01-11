package org.seniorsigan.zkpauthenticatorclient.impl.schnorr

import org.seniorsigan.zkpauth.lib.SchnorrSignature
import org.seniorsigan.zkpauthenticator.*
import org.seniorsigan.zkpauthenticatorclient.CommonResponse
import org.seniorsigan.zkpauthenticatorclient.LoginModel
import org.seniorsigan.zkpauthenticatorclient.impl.ObjectConverter
import java.math.BigInteger
import java.security.SecureRandom

class SchnorrAuthenticator(
        repository: UserRepository,
        transport: Transport,
        private val converter: ObjectConverter,
        private val secureRandom: SecureRandom,
        private val schnorrSignature: SchnorrSignature
): Authenticator(repository, transport) {
    override val algorithmName: String = "schnorr"

    override fun signup(token: Token, username: String, callback: AuthenticatorCallback) {
        val keyPair = schnorrSignature.generateKey()
        val model = SchnorrSignupModel(username, keyPair.public, token.token)

        transport.send(converter.to(model), token.address(), object : TransportCallback {
            override fun onSuccess(data: String) {
                val response = converter.from(data, CommonResponse::class.java)
                if (response != null && response.success) {
                    createUser(token, username, keyPair)
                    callback.onSuccess("User $username@${token.domainName} signed up")
                } else {
                    callback.onFailure("Server respond with error $data", null)
                }
            }

            override fun onFailure(throwable: Throwable) {
                callback.onFailure("Transport error", throwable)
            }
        })
    }

    override fun login(token: Token, username: String, callback: AuthenticatorCallback) {
        val user = repository.find(token.domainName, username)
        if (user == null) {
            callback.onFailure("User $username:${token.domainName}", null)
            return
        }
        val secret = converter.from(user.secret, SchnorrSignature.SchnorrKeyPair::class.java)
        if (secret == null) {
            callback.onFailure("Can't parse secret for user $user with algorithm $algorithmName", null)
            return
        }
        val sessionPair = schnorrSignature.generateSessionPair(secret.public)
        val model = LoginModel(username, sessionPair.public().toString(), token.token)

        transport.send(converter.to(model), token.address(), object : TransportCallback {
            override fun onSuccess(data: String) {
                val response = converter.from(data, CommonResponse::class.java)
                if (response != null && response.success) {
                    var nonce: BigInteger
                    try {
                        nonce = BigInteger(response.message)
                    } catch (e: Exception) {
                        callback.onFailure("Can't parse nonce from server: ${e.message}", e)
                        return
                    }
                    val s = schnorrSignature.calculateS(sessionPair, secret, nonce)
                    val modelWithS = LoginModel(username, s.toString(), token.token)
                    transport.send(converter.to(modelWithS), token.address(), object : TransportCallback {
                        override fun onSuccess(data: String) {
                            val finalResponse = converter.from(data, CommonResponse::class.java)
                            if (finalResponse != null && finalResponse.success) {
                                callback.onSuccess("User $username logged in ${token.domainName} with algorithm $algorithmName")
                            } else {
                                callback.onFailure("Server respond with error $finalResponse", null)
                            }
                        }

                        override fun onFailure(throwable: Throwable) {
                            callback.onFailure("Transport error: ${throwable.message}", throwable)
                        }
                    })
                } else {
                    callback.onFailure("Server respond with error $response", null)
                }
            }

            override fun onFailure(throwable: Throwable) {
                callback.onFailure("Transport error: ${throwable.message}", throwable)
            }
        })
    }

    private fun createUser(token: Token, username: String, keyPair: SchnorrSignature.SchnorrKeyPair) {
        var secretJson = converter.to(keyPair)
        repository.create(username, token.domainName, algorithmName, secretJson)
    }

}
