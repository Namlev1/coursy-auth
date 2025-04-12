package com.coursy.masterauthservice.failure

sealed class JwtFailure : Failure {
    data class InvalidToken(val token: String) : JwtFailure()

    override fun message(): String = when (this) {
        is InvalidToken -> "Invalid JWT token \"$token\""
    }
}
