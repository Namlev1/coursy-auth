package com.coursy.auth.failure

sealed class AuthorizationFailure : Failure {
    data object UserSuspended : AuthorizationFailure()

    override fun message(): String = when (this) {
        is UserSuspended -> "User account has been suspended"
    }
}