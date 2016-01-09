package org.seniorsigan.zkpauthenticator

class AuthenticatorBuilder {
    private val authenticators: MutableMap<String, Authenticator> = hashMapOf()

    fun register(authenticator: Authenticator): AuthenticatorBuilder {
        authenticators[authenticator.algorithmName] = authenticator
        return this
    }

    fun get(algorithmName: String): Authenticator {
        return authenticators[algorithmName] ?: throw Exception("Authenticator for algorithm $algorithmName doesn't exist")
    }
}