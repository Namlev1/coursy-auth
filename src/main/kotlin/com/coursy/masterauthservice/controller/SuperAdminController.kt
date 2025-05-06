package com.coursy.masterauthservice.controller

import arrow.core.flatMap
import com.coursy.masterauthservice.dto.ChangePasswordRequest
import com.coursy.masterauthservice.dto.RegistrationRequest
import com.coursy.masterauthservice.dto.UserUpdateRequest
import com.coursy.masterauthservice.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/super-admin")
@RestController
class SuperAdminController(
    private val userService: UserService,
    private val httpFailureResolver: HttpFailureResolver
) {
    @PostMapping
    fun createSuperUser(@RequestBody request: RegistrationRequest): ResponseEntity<Any> {
        val result = request
            .validate()
            .flatMap { validated ->
                userService.createUser(validated)
            }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { ResponseEntity.status(HttpStatus.CREATED).build() }
        )
    }

    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody request: UserUpdateRequest): ResponseEntity<Any> {
        val result = request
            .validate()
            .flatMap { validated ->
                userService.updateUser(
                    id,
                    validated,
                    isRegularUser = false,
                )
            }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { response -> ResponseEntity.status(HttpStatus.OK).body(response) }
        )
    }

    @PutMapping("/{id}/password")
    fun updateUserPassword(@PathVariable id: Long, @RequestBody request: ChangePasswordRequest): ResponseEntity<Any> {
        val result = request
            .validate()
            .flatMap { validated ->
                userService.updatePassword(
                    userId = id,
                    request = validated,
                    isRegularUser = false
                )
            }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { ResponseEntity.status(HttpStatus.OK).build() }
        )
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Any> {
        return userService
            .removeUser(
                id = id,
                isRegularUser = false
            )
            .fold(
                { failure -> httpFailureResolver.handleFailure(failure) },
                { ResponseEntity.status(HttpStatus.NO_CONTENT).build() }
            )
    }
}