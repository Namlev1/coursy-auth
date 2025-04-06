package com.coursy.masterauthservice.failure

sealed class CompanyNameFailure : Failure {
    data object Empty : CompanyNameFailure()
    data object InvalidFormat : CompanyNameFailure()
    data class TooShort(val minLength: Int) : CompanyNameFailure()
    data class TooLong(val maxLength: Int) : CompanyNameFailure()

    override fun message(): String = when (this) {
        Empty -> "Company name cannot be empty"
        InvalidFormat -> "Company name format is invalid"
        is TooLong -> "Company name is too long (maximum length: $maxLength)"
        is TooShort -> "Company name is too short (minimum length: $minLength)"
    }
}