package com.coursy.masterauthservice.type

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.masterauthservice.failure.CompanyNameFailure

@JvmInline
value class CompanyName private constructor(val value: String) {
    companion object {
        private const val MIN_LENGTH = 2
        private const val MAX_LENGTH = 100

        private fun isValidCompanyNameChar(c: Char): Boolean {
            return c.isLetterOrDigit() || c == ' ' || c == '-' || c == '\'' ||
                    c == '&' || c == '.' || c == ',' || c == '@'
        }

        fun create(value: String): Either<CompanyNameFailure, CompanyName> = when {
            value.isBlank() -> CompanyNameFailure.Empty.left()
            value.length < MIN_LENGTH -> CompanyNameFailure.TooShort(MIN_LENGTH).left()
            value.length > MAX_LENGTH -> CompanyNameFailure.TooLong(MAX_LENGTH).left()
            !value.all { isValidCompanyNameChar(it) } -> CompanyNameFailure.InvalidFormat.left()
            else -> CompanyName(value).right()
        }
    }
}