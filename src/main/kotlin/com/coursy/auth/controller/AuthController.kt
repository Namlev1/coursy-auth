package com.coursy.auth.controller

import arrow.core.flatMap
import com.coursy.auth.dto.LoginRequest
import com.coursy.auth.dto.RefreshJwtRequest
import com.coursy.auth.dto.RegistrationRequest
import com.coursy.auth.repository.UserRepository
import com.coursy.auth.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

// TODO: When USERS registers new account, it must send message to AUTH
//  in order to create new auth record in db.
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val httpFailureResolver: HttpFailureResolver,
    private val userRepository: UserRepository,
    private val encoder: PasswordEncoder,
) {
    @PostMapping("/login")
    fun authenticateUser(@RequestBody request: LoginRequest): ResponseEntity<Any> {
        val result = request
            .validate()
            .flatMap { validated -> authService.authenticateUser(validated) }

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


    // TODO: make this endpoint only available internally
    @PostMapping("/register")
    fun registerUser(@RequestBody request: RegistrationRequest): ResponseEntity<Any> {
        return authService
            .registerUser(request)
            .fold(
                { failure -> return httpFailureResolver.handleFailure(failure) },
                { ResponseEntity.status(HttpStatus.CREATED).build() }
            )
    }
}