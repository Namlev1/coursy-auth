package com.coursy.masterauthservice.controller

import com.coursy.masterauthservice.dto.JwtResponse
import com.coursy.masterauthservice.dto.LoginRequest
import com.coursy.masterauthservice.dto.RegistrationRequest
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
    fun createUser(@RequestBody request: RegistrationRequest) = userService.createUser(request).fold(
        { failure -> handleFailure(failure) },
        { success -> ResponseEntity.status(HttpStatus.CREATED).build() }
    )

    @PostMapping("/login")
    fun authenticateUser(@RequestBody loginRequest: LoginRequest): ResponseEntity<JwtResponse> {
        val jwtResponse = userService.authenticateUser(loginRequest)
        return ResponseEntity.ok(jwtResponse)
    }

    @GetMapping("/secret")
    fun authorizedEndpoint() = "You passed the authorization flow!"

    private fun handleFailure(failure: RoleFailure) =
        when (failure) {
            is RoleFailure.NotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure.name)
        }
}