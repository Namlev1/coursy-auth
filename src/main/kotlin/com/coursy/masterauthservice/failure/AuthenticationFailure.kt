package com.coursy.masterauthservice.failure

sealed class AuthenticationFailure : Failure {
    data object InvalidCredentials : AuthenticationFailure()

    override fun message(): String = when (this) {
        InvalidCredentials -> "Invalid email or password"
    }
}