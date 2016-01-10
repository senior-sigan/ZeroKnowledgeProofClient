package org.seniorsigan.zkpauthenticatorclient.impl.schnorr

import org.seniorsigan.zkpauth.lib.SchnorrSignature

data class SchnorrSignupModel(
        val login: String,
        val key: SchnorrSignature.SchnorrPublicKey,
        val token: String
)