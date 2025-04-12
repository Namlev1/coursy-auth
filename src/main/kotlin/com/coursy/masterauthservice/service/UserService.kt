package com.coursy.masterauthservice.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.masterauthservice.dto.RegistrationRequest
import com.coursy.masterauthservice.failure.Failure
import com.coursy.masterauthservice.failure.RoleFailure
import com.coursy.masterauthservice.model.User
import com.coursy.masterauthservice.repository.RoleRepository
import com.coursy.masterauthservice.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun createUser(request: RegistrationRequest.Validated): Either<Failure, Unit> {
        val role =
            roleRepository.findByName(request.roleName)
                ?: return RoleFailure.NotFound.left()

        // TODO implement better handling
        val encryptedPassword = passwordEncoder.encode(request.password.value)
        val user = User(
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            password = encryptedPassword,
            companyName = request.companyName,
            role = role
        )
        userRepository.save(user)
        return Unit.right()
    }

}