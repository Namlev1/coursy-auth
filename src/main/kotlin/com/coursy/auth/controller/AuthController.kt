package com.coursy.auth.controller

import arrow.core.flatMap
import com.coursy.auth.dto.LoginRequest
import com.coursy.auth.dto.RefreshJwtRequest
import com.coursy.auth.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val httpFailureResolver: HttpFailureResolver
) {
    @PostMapping("/login")
    fun authenticateUser(@RequestBody request: LoginRequest): ResponseEntity<Any> {
        val result = request.validate().flatMap { validated -> authService.authenticateUser(validated) }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { jwtResponse -> ResponseEntity.status(HttpStatus.OK).body(jwtResponse) }
        )
    }

    @GetMapping("/refresh")
    fun refreshJwt(@RequestBody request: RefreshJwtRequest): ResponseEntity<Any> {
        val result = request.validate().flatMap { validated -> authService.refreshJwtToken(validated) }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { jwtResponse -> ResponseEntity.status(HttpStatus.OK).body(jwtResponse) }
        )
    }
    
}