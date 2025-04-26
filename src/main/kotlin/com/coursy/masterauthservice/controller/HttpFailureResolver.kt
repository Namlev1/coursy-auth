package com.coursy.masterauthservice.controller

import com.coursy.masterauthservice.failure.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class HttpFailureResolver {
    fun handleFailure(failure: Failure): ResponseEntity<Any> =
        when (failure) {
            is UserFailure.EmailAlreadyExists -> ResponseEntity.status(HttpStatus.CONFLICT).body(failure.message())
            is UserFailure.IdNotExists -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure.message())
            is RoleFailure.NotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure.message())
            is RefreshTokenFailure.Empty -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failure.message())
            is RefreshTokenFailure.NotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure.message())
            is RefreshTokenFailure.Expired -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(failure.message())
            is AuthorizationFailure.InsufficientRole -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(failure.message())

            is AuthorizationFailure.UserSuspended -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(failure.message())
            else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failure.message())
        }
}