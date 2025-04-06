package com.coursy.masterauthservice.type

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.masterauthservice.failure.EmailFailure

@JvmInline
value class Email private constructor(val value: String) {
    companion object {
        private val EMAIL_REGEX = Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
        private const val MIN_LENGTH = 6
        private const val MAX_LENGTH = 60

        fun create(value: String): Either<EmailFailure, Email> = when {
            value.isEmpty() -> EmailFailure.Empty.left()
            "@" !in value -> EmailFailure.MissingAtSymbol.left()
            value.length < MIN_LENGTH -> EmailFailure.TooShort(MIN_LENGTH).left()
            value.length > MAX_LENGTH -> EmailFailure.TooLong(MIN_LENGTH).left()
            !value.matches(EMAIL_REGEX) -> EmailFailure.InvalidFormat.left()
            else -> Email(value).right()
        }
    }
}