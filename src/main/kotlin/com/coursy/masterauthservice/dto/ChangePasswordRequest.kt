package com.coursy.masterauthservice.dto

import arrow.core.Either
import com.coursy.masterauthservice.failure.Failure
import com.coursy.masterauthservice.type.Password

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