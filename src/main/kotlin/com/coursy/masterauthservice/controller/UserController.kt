package com.coursy.masterauthservice.controller

import arrow.core.flatMap
import com.coursy.masterauthservice.dto.ChangePasswordRequest
import com.coursy.masterauthservice.dto.RegistrationRequest
import com.coursy.masterauthservice.model.RoleName
import com.coursy.masterauthservice.security.UserDetailsImp
import com.coursy.masterauthservice.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RequestMapping("/user")
@RestController
class UserController(
    private val userService: UserService,
    private val httpFailureResolver: HttpFailureResolver
) {
    @GetMapping("/me")
    fun getCurrentUser(@AuthenticationPrincipal currentUser: UserDetailsImp): ResponseEntity<Any> {
        return userService
            .getUser(currentUser.id)
            .fold(
                { failure -> httpFailureResolver.handleFailure(failure) },
                { response -> ResponseEntity.status(HttpStatus.OK).body(response) }
            )
    }

    @PostMapping
    fun createRegularUser(@RequestBody request: RegistrationRequest): ResponseEntity<Any> {
        val result = request
            .validate()
            .map { validated -> validated.copy(roleName = RoleName.ROLE_USER) }
            .flatMap { validated -> userService.createUser(validated) }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { ResponseEntity.status(HttpStatus.CREATED).build() }
        )
    }

    @PutMapping("/me/password")
    fun updateCurrentUserPassword(
        @AuthenticationPrincipal currentUser: UserDetailsImp,
        @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<Any> {
        val result = request
            .validate()
            .flatMap { validated ->
                userService.updatePassword(currentUser.id, validated)
            }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { ResponseEntity.status(HttpStatus.OK).build() }
        )
    }
}