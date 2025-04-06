package com.coursy.masterauthservice.failure

sealed class PasswordFailure : Failure {
    data object Empty : PasswordFailure()
    data class TooShort(val minLength: Int) : PasswordFailure()
    data class TooLong(val maxLength: Int) : PasswordFailure()
    data class InsufficientComplexity(val errors: List<ComplexityFailure>) : PasswordFailure()
    data object RepeatingCharacters : PasswordFailure()

    sealed class ComplexityFailure {
        data object MissingUppercase : ComplexityFailure()
        data object MissingLowercase : ComplexityFailure()
        data object MissingDigit : ComplexityFailure()
        data object MissingSpecialChar : ComplexityFailure()
    }

    override fun message(): String = when (this) {
        Empty -> "Password cannot be empty"
        is TooShort -> "Password is too short (minimum length: $minLength characters)"
        is TooLong -> "Password is too long (maximum length: $maxLength characters)"
        is InsufficientComplexity -> buildComplexityErrorMessage(errors)
        RepeatingCharacters -> "Password contains repeating characters (e.g., 'aaa')"
    }

    private fun buildComplexityErrorMessage(errors: List<ComplexityFailure>): String {
        val requirements = errors.map { error ->
            when (error) {
                ComplexityFailure.MissingUppercase -> "at least one uppercase letter"
                ComplexityFailure.MissingLowercase -> "at least one lowercase letter"
                ComplexityFailure.MissingDigit -> "at least one digit"
                ComplexityFailure.MissingSpecialChar -> "at least one special character"
            }
        }

        return "Password must contain ${requirements.joinToString(", ")}"
    }
}