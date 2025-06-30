package com.coursy.auth.controller

import arrow.core.flatMap
import com.coursy.auth.dto.LoginRequest
import com.coursy.auth.dto.RefreshJwtRequest
import com.coursy.auth.model.User
import com.coursy.auth.repository.UserRepository
import com.coursy.auth.service.AuthService
import com.coursy.auth.type.Email
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
    // TODO remove at production
    @PostMapping("/test-add")
    fun addtodb() = userRepository.save(
        User(
            id = 0,
            email = Email.create("email@email.com").getOrNull()!!,
            password = encoder.encode("pa##w0RD")
        )
    )
    
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