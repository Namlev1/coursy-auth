package com.coursy.auth.failure

sealed class AuthorizationFailure : Failure {
    data object InsufficientRole : AuthorizationFailure()
    data object UserSuspended : AuthorizationFailure()

    override fun message(): String = when (this) {
        is InsufficientRole -> "User doesn't have the required role for this operation"
        is UserSuspended -> "User account has been suspended"
    }
}