package com.coursy.masterauthservice.failure

sealed class EmailFailure : Failure {
    data object Empty : EmailFailure()
    data object MissingAtSymbol : EmailFailure()
    data object InvalidFormat : EmailFailure()
    data class TooShort(val minLength: Int) : EmailFailure()
    data class TooLong(val maxLength: Int) : EmailFailure()

    override fun message(): String = when (this) {
        Empty -> "Email cannot be empty"
        MissingAtSymbol -> "Email must contain an @ symbol"
        InvalidFormat -> "Email format is invalid"
        is TooLong -> "Email is too long (maximum length: $maxLength)"
        is TooShort -> "Email is too short (minimum length: $minLength)"
    }
}