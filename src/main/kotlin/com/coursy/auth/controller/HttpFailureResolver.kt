package com.coursy.auth.controller

import com.coursy.auth.failure.AuthorizationFailure
import com.coursy.auth.failure.Failure
import com.coursy.auth.failure.RefreshTokenFailure
import com.coursy.auth.failure.UserFailure
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class HttpFailureResolver {
    fun handleFailure(failure: Failure): ResponseEntity<Any> =
        when (failure) {
            is UserFailure.IdNotExists -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure.message())
            is RefreshTokenFailure.Empty -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failure.message())
            is RefreshTokenFailure.NotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure.message())
            is RefreshTokenFailure.Expired -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(failure.message())

            is AuthorizationFailure.UserSuspended -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(failure.message())
            else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failure.message())
        }
}