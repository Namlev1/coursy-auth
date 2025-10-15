package com.coursy.auth.controller

import arrow.core.flatMap
import com.coursy.auth.dto.LoginRequest
import com.coursy.auth.dto.RefreshJwtRequest
import com.coursy.auth.dto.RegistrationRequest
import com.coursy.auth.repository.UserRepository
import com.coursy.auth.service.AuthService
import com.coursy.auth.type.Email
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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

    @PostMapping("/logout")
    fun logoutUser(
        authentication: Authentication
    ): ResponseEntity<Any> {
        val email = authentication.principal as Email
        return authService.logoutUser(email)
            .fold(
                { failure -> httpFailureResolver.handleFailure(failure) },
                { ResponseEntity.status(HttpStatus.OK).build() }
            )
    }

    @PostMapping("/refresh")
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