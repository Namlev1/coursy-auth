package com.coursy.masterauthservice.controller

import com.coursy.masterauthservice.failure.Failure
import com.coursy.masterauthservice.failure.RoleFailure
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class HttpFailureResolver {
    fun handleFailure(failure: Failure): ResponseEntity<Any> =
        when (failure) {
            is RoleFailure.NotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure.message())
            else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failure.message())
        }
}