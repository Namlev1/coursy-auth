package com.coursy.masterauthservice.controller

import arrow.core.flatMap
import arrow.core.left
import com.coursy.masterauthservice.dto.ChangePasswordRequest
import com.coursy.masterauthservice.dto.RegistrationRequest
import com.coursy.masterauthservice.dto.UserUpdateRequest
import com.coursy.masterauthservice.failure.AuthorizationFailure
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

    @PostMapping("/admin")
    fun createUser(@RequestBody request: RegistrationRequest): ResponseEntity<Any> {
        val result = request
            .validate()
            .flatMap { validated ->
                if (validated.roleName == RoleName.ROLE_SUPER_ADMIN)
                    AuthorizationFailure.UserSuspended.left()
                userService.createUser(validated)
            }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { ResponseEntity.status(HttpStatus.CREATED).build() }
        )
    }

    @PostMapping("/super-admin")
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

    @GetMapping("admin/{id}")
    fun getUser(@PathVariable id: Long): ResponseEntity<Any> {
        return userService
            .getUser(id)
            .fold(
                { failure -> httpFailureResolver.handleFailure(failure) },
                { response -> ResponseEntity.status(HttpStatus.OK).body(response) }
            )
    }

    @PutMapping("/admin/{id}")
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

    @PutMapping("/super-admin/{id}")
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

    @PutMapping("/admin/{id}/password")
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

    @PutMapping("/super-admin/{id}/password")
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


    @DeleteMapping("/admin/{id}")
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

    @DeleteMapping("/super-admin/{id}")
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