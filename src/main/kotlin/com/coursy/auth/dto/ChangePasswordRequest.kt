package com.coursy.auth.dto

import arrow.core.Either
import com.coursy.auth.failure.Failure
import com.coursy.auth.type.Password

data class ChangePasswordRequest(
    val password: String
) : SelfValidating<Failure, ChangePasswordRequest.Validated> {
    data class Validated(
        val password: Password
    )

    override fun validate(): Either<Failure, Validated> {
        return Password.create(password)
            .map { password -> Validated(password = password) }
            .mapLeft { failure -> failure }
    }
}