package org.seniorsigan.zkpauthenticator

abstract class Authenticator(
        protected val repository: UserRepository,
        protected val transport: Transport
) {
    abstract val algorithmName: String
    abstract fun signup(token: Token, username: String, callback: AuthenticatorCallback)
    abstract fun login(token: Token, username: String, callback: AuthenticatorCallback)

    public interface AuthenticatorCallback {
        fun onSuccess(message: String)
        fun onFailure(message: String, throwable: Throwable?)
    }
}