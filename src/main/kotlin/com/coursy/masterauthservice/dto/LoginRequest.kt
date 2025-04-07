package com.coursy.masterauthservice.dto

import arrow.core.Either
import arrow.core.getOrElse
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
        val email = Email.create(email).getOrElse { return it.left() }
        val password = Password.create(password).getOrElse { return it.left() }

        return Validated(
            email = email,
            password = password
        ).right()
    }
}
