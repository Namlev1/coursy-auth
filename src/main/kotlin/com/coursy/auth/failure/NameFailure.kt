package com.coursy.auth.failure

sealed class NameFailure : Failure {
    data object Empty : NameFailure()
    data object InvalidFormat : NameFailure()
    data class TooShort(val minLength: Int) : NameFailure()
    data class TooLong(val maxLength: Int) : NameFailure()

    override fun message(): String = when (this) {
        Empty -> "Name cannot be empty"
        InvalidFormat -> "Name format is invalid"
        is TooLong -> "Name is too long (maximum length: $maxLength)"
        is TooShort -> "Name is too short (minimum length: $minLength)"
    }
}