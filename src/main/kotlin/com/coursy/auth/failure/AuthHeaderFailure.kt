package com.coursy.auth.failure

sealed class AuthHeaderFailure : Failure {
    data object MissingHeader : AuthHeaderFailure()
    data class InvalidHeaderFormat(val header: String) : AuthHeaderFailure()

    override fun message(): String = when (this) {
        MissingHeader -> "Missing Authorization header"
        is InvalidHeaderFormat -> "Invalid Authorization header format: $header"
    }
}