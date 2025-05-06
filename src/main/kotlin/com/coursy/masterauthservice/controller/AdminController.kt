package com.coursy.masterauthservice.controller

import arrow.core.flatMap
import arrow.core.left
import com.coursy.masterauthservice.dto.ChangePasswordRequest
import com.coursy.masterauthservice.dto.RegistrationRequest
import com.coursy.masterauthservice.dto.UserUpdateRequest
import com.coursy.masterauthservice.failure.AuthorizationFailure
import com.coursy.masterauthservice.model.RoleName
import com.coursy.masterauthservice.service.UserService
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/admin")
@RestController
class AdminController(
    private val userService: UserService,
    private val httpFailureResolver: HttpFailureResolver
) {
    @PostMapping
    fun createUser(@RequestBody request: RegistrationRequest): ResponseEntity<Any> {
        val result = request
            .validate()
            .flatMap { validated ->
                if (validated.roleName == RoleName.ROLE_SUPER_ADMIN)
                    AuthorizationFailure.InsufficientRole.left()
                else
                    userService.createUser(validated)
            }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { ResponseEntity.status(HttpStatus.CREATED).build() }
        )
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ResponseEntity<Any> {
        return userService
            .getUser(id)
            .fold(
                { failure -> httpFailureResolver.handleFailure(failure) },
                { response -> ResponseEntity.status(HttpStatus.OK).body(response) }
            )
    }

    @GetMapping
    fun getAllUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<Any> =
        when {
            arePageParamsInvalid(page, size) -> ResponseEntity.badRequest().build()
            else -> PageRequest.of(page, size)
                .let { userService.getUserPage(it) }
                .let { ResponseEntity.ok(it) }
        }

    @PutMapping("/{id}")
    fun updateRegularUser(@PathVariable id: Long, @RequestBody request: UserUpdateRequest): ResponseEntity<Any> {
        val result = request
            .validate()
            .flatMap { validated ->
                userService.updateUser(id, validated)
            }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { response -> ResponseEntity.status(HttpStatus.OK).body(response) }
        )
    }

    @PutMapping("/{id}/password")
    fun updateRegularUserPassword(
        @PathVariable id: Long,
        @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<Any> {
        val result = request
            .validate()
            .flatMap { validated ->
                userService.updatePassword(id, validated)
            }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { ResponseEntity.status(HttpStatus.OK).build() }
        )
    }

    @DeleteMapping("/{id}")
    fun deleteRegularUser(@PathVariable id: Long): ResponseEntity<Any> {
        return userService
            .removeUser(
                id = id,
                isRegularUser = true
            )
            .fold(
                { failure -> httpFailureResolver.handleFailure(failure) },
                { ResponseEntity.status(HttpStatus.NO_CONTENT).build() }
            )
    }

    private fun arePageParamsInvalid(page: Int, size: Int) =
        page < 0 || size <= 0
}