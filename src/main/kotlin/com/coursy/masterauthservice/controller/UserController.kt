package com.coursy.masterauthservice.controller

import arrow.core.flatMap
import com.coursy.masterauthservice.dto.RegistrationRequest
import com.coursy.masterauthservice.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/user")
@RestController
class UserController(
    private val userService: UserService,
    private val httpFailureResolver: HttpFailureResolver
) {
    @PostMapping
    fun createUser(@RequestBody request: RegistrationRequest): ResponseEntity<Any> {
        val result = request.validate().flatMap { validated -> userService.createUser(validated) }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { ResponseEntity.status(HttpStatus.CREATED).build() }
        )
    }

}