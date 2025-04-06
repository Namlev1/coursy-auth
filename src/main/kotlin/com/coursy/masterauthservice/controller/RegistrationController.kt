package com.coursy.masterauthservice.controller

import arrow.core.flatMap
import com.coursy.masterauthservice.dto.JwtResponse
import com.coursy.masterauthservice.dto.LoginRequest
import com.coursy.masterauthservice.dto.RegistrationRequest
import com.coursy.masterauthservice.failure.Failure
import com.coursy.masterauthservice.failure.RoleFailure
import com.coursy.masterauthservice.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/register")
@RestController
class RegistrationController(
    private val userService: UserService
) {
    @PostMapping
    fun createUser(@RequestBody request: RegistrationRequest): ResponseEntity<Any> {
        val result = request.validate().flatMap { validated -> userService.createUser(validated) }

        return result.fold(
            { failure -> handleFailure(failure) },
            { ResponseEntity.status(HttpStatus.CREATED).build() }
        )
    }

    @PostMapping("/login")
    fun authenticateUser(@RequestBody loginRequest: LoginRequest): ResponseEntity<JwtResponse> {
        val jwtResponse = userService.authenticateUser(loginRequest)
        return ResponseEntity.ok(jwtResponse)
    }

    @GetMapping("/secret")
    fun authorizedEndpoint() = "You passed the authorization flow!"

    // todo extract to http resolver
    private fun handleFailure(failure: Failure): ResponseEntity<Any> =
        when (failure) {
//            is RoleFailure.NotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure.name)
            is RoleFailure.NotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure.message())
            else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failure.message())
        }
}