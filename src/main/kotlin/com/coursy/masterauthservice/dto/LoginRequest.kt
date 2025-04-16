package com.coursy.masterauthservice.dto

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.masterauthservice.failure.Failure
import com.coursy.masterauthservice.type.Email
import com.coursy.masterauthservice.type.Password

data class LoginRequest(
    val email: String,
    val password: String
) : SelfValidating<Failure, LoginRequest.Validated> {
    data class Validated(
        val email: Email,
        val password: Password
    )

    override fun validate(): Either<Failure, Validated> {
        val emailResult = Email.create(email)
        val passwordResult = Password.create(password)

        val firstError = listOfNotNull(
            emailResult.leftOrNull(),
            passwordResult.leftOrNull()
        ).firstOrNull()

        return firstError?.left() ?: Validated(
            email = emailResult.getOrNull()!!,
            password = passwordResult.getOrNull()!!
        ).right()
    }
}
