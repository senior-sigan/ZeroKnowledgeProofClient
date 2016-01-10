package org.seniorsigan.zkpauthenticatorclient.impl.schnorr

import org.seniorsigan.zkpauth.lib.SchnorrSignature
import org.seniorsigan.zkpauthenticator.*
import org.seniorsigan.zkpauthenticatorclient.CommonResponse
import org.seniorsigan.zkpauthenticatorclient.impl.ObjectConverter
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
        throw UnsupportedOperationException()
    }

    private fun createUser(token: Token, username: String, keyPair: SchnorrSignature.SchnorrKeyPair) {
        var secretJson = converter.to(keyPair)
        repository.create(username, token.domainName, algorithmName, secretJson)
    }

}
