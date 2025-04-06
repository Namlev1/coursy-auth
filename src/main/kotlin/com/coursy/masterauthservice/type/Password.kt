package com.coursy.masterauthservice.type

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.masterauthservice.failure.PasswordFailure

@JvmInline
value class Password private constructor(private val value: String) {
    companion object {
        private const val MIN_LENGTH = 8
        private const val MAX_LENGTH = 72
        private val UPPERCASE_REGEX = Regex("[A-Z]")
        private val LOWERCASE_REGEX = Regex("[a-z]")
        private val DIGIT_REGEX = Regex("[0-9]")
        private val SPECIAL_CHAR_REGEX = Regex("[^A-Za-z0-9]")
        private val REPEATING_CHARS_REGEX = Regex("(.)\\1{2,}")  // Three or more consecutive same characters

        fun create(value: String): Either<PasswordFailure, Password> {
            if (value.isEmpty()) return PasswordFailure.Empty.left()
            if (value.length < MIN_LENGTH) return PasswordFailure.TooShort(MIN_LENGTH).left()
            if (value.length > MAX_LENGTH) return PasswordFailure.TooLong(MAX_LENGTH).left()

            val complexityErrors = mutableListOf<PasswordFailure.ComplexityFailure>()

            if (!value.contains(UPPERCASE_REGEX)) {
                complexityErrors.add(PasswordFailure.ComplexityFailure.MissingUppercase)
            }

            if (!value.contains(LOWERCASE_REGEX)) {
                complexityErrors.add(PasswordFailure.ComplexityFailure.MissingLowercase)
            }

            if (!value.contains(DIGIT_REGEX)) {
                complexityErrors.add(PasswordFailure.ComplexityFailure.MissingDigit)
            }

            if (!value.contains(SPECIAL_CHAR_REGEX)) {
                complexityErrors.add(PasswordFailure.ComplexityFailure.MissingSpecialChar)
            }

            if (complexityErrors.isNotEmpty()) {
                return PasswordFailure.InsufficientComplexity(complexityErrors).left()
            }

            if (value.contains(REPEATING_CHARS_REGEX)) {
                return PasswordFailure.RepeatingCharacters.left()
            }

            return Password(value).right()
        }
    }

    override fun toString(): String = value
}