package org.seniorsigan.zkpauthenticatorclient.impl.skey

import org.seniorsigan.zkpauthenticator.*
import org.seniorsigan.zkpauthenticatorclient.*
import org.seniorsigan.zkpauthenticatorclient.impl.ObjectConverter
import java.security.SecureRandom

class SKeyAuthenticator(
        repository: UserRepository,
        transport: Transport,
        private val keysGenerator: KeysGenerator,
        private val converter: ObjectConverter,
        private val secureRandom: SecureRandom
): Authenticator(repository, transport) {
    val keysAmount = 1000
    override val algorithmName: String = "s/key"

    override fun signup(token: Token, username: String, callback: AuthenticatorCallback) {
        val keys = keysGenerator.generateSequence(nonce(), keysAmount)
        val model = SignupModel(username, keys.last(), token.token)

        transport.send(converter.to(model), token.address(), object : TransportCallback {
            override fun onSuccess(data: String) {
                val response = converter.from(data, CommonResponse::class.java)
                if (response != null && response.success) {
                    createUser(token, username, keys)
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
        val secret = converter.from(user.secret, SKeySecret::class.java)
        if (secret == null) {
            callback.onFailure("Can't parse secret for user $user", null)
            return
        }
        val key = secret.current()
        if (key == null) {
            callback.onFailure("Can't get current s-key for user $user: index out of bound", null)
            return
        }
        val model = LoginModel(username, key, token.token)


        transport.send(converter.to(model), token.address(), object : TransportCallback {
            override fun onSuccess(data: String) {
                val response = converter.from(data, CommonResponse::class.java)
                if (response != null && response.success) {
                    try {
                        updateSecret(user, secret)
                    } catch (e: Exception) {
                        callback.onFailure("Can't update user $user: ${e.message}", e)
                        return
                    }
                    callback.onSuccess("User $username logged in ${token.domainName}")
                } else {
                    callback.onFailure("Server respond with error $response", null)
                }
            }

            override fun onFailure(throwable: Throwable) {
                callback.onFailure("Transport error", throwable)
            }
        })
    }

    private fun updateSecret(user: UserModel, secret: SKeySecret) {
        val updatedSecret = SKeySecret(secret.keys, secret.currentKey - 1)
        user.secret = converter.to(updatedSecret)
        repository.update(user)
    }

    private fun createUser(token: Token, username: String, keys: List<String>) {
        val secret = SKeySecret(keys, keys.size - 2)
        var secretJson = converter.to(secret)
        repository.create(username, token.domainName, algorithmName, secretJson)
    }

    private fun nonce(): String {
        val bytes = ByteArray(128)
        secureRandom.nextBytes(bytes)
        return bytes.toHexString()
    }
}