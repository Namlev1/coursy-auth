package com.coursy.auth.controller

import arrow.core.flatMap
import com.auth0.jwt.interfaces.DecodedJWT
import com.coursy.auth.dto.LoginRequest
import com.coursy.auth.dto.OwnerRegistrationRequest
import com.coursy.auth.dto.RefreshJwtRequest
import com.coursy.auth.dto.RegistrationRequest
import com.coursy.auth.repository.UserRepository
import com.coursy.auth.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

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
        val jwt = authentication.credentials as DecodedJWT
        val id = UUID.fromString(jwt.getClaim("id").asString())
        return authService.logoutUser(id)
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


    @PostMapping("/register")
    fun registerUser(@RequestBody request: RegistrationRequest): ResponseEntity<Any> {
        return authService
            .registerUser(request)
            .fold(
                { failure -> return httpFailureResolver.handleFailure(failure) },
                { ResponseEntity.status(HttpStatus.CREATED).build() }
            )
    }

    @PostMapping("/owner")
    fun createOwner(@RequestBody request: OwnerRegistrationRequest): ResponseEntity<Any> {
        return authService
            .createOwner(request)
            .fold(
                { failure -> return httpFailureResolver.handleFailure(failure) },
                { ResponseEntity.status(HttpStatus.CREATED).build() }
            )
    }
}