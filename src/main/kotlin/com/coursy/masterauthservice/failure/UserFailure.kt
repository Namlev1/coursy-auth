package com.coursy.masterauthservice.failure

sealed class UserFailure : Failure {
    data object EmailAlreadyExists : UserFailure()

    override fun message(): String = when (this) {
        EmailAlreadyExists -> "User with this email already exists."
    }
}