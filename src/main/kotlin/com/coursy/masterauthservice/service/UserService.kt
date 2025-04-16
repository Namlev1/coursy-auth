package com.coursy.masterauthservice.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.masterauthservice.dto.*
import com.coursy.masterauthservice.failure.Failure
import com.coursy.masterauthservice.failure.RoleFailure
import com.coursy.masterauthservice.failure.UserFailure
import com.coursy.masterauthservice.model.Role
import com.coursy.masterauthservice.model.User
import com.coursy.masterauthservice.repository.RoleRepository
import com.coursy.masterauthservice.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun createUser(request: RegistrationRequest.Validated): Either<Failure, Unit> {
        if (userRepository.existsByEmail(request.email)) {
            return UserFailure.EmailAlreadyExists.left()
        }

        val role =
            roleRepository.findByName(request.roleName)
                ?: return RoleFailure.NotFound.left()

        val user = createUser(request, role)
        userRepository.save(user)
        return Unit.right()
    }

    fun removeUser(id: Long): Either<Failure, Unit> {
        if (!userRepository.existsById(id)) {
            return UserFailure.IdNotExists.left()
        }

        userRepository.removeUserById(id)
        return Unit.right()
    }

    fun getUser(id: Long): Either<Failure, UserResponse> {
        return userRepository.findById(id)
            .map { it.toUserResponse().right() }
            .getOrElse { UserFailure.IdNotExists.left() }
    }

    fun updateUser(userId: Long, request: UserUpdateRequest.Validated): Either<Failure, UserResponse> {
        val userOption = userRepository.findById(userId)

        if (userOption.isEmpty) {
            return UserFailure.IdNotExists.left()
        }

        val user = userOption.get()

        if (request.roleName != null) {
            val role = roleRepository.findByName(request.roleName)
                ?: return RoleFailure.NotFound.left()
            user.role = role
        }

        request.firstName?.let { user.firstName = it }
        request.lastName?.let { user.lastName = it }
        request.companyName?.let { user.companyName = it }

        return userRepository.save(user).toUserResponse().right()
    }

    fun updatePassword(userId: Long, request: ChangePasswordRequest.Validated): Either<Failure, Unit> {
        val userOption = userRepository.findById(userId)

        if (userOption.isEmpty) {
            return UserFailure.IdNotExists.left()
        }

        val user = userOption.get()
        user.password = passwordEncoder.encode(request.password.value)
        userRepository.save(user)
        return Unit.right()
    }

    private fun createUser(request: RegistrationRequest.Validated, role: Role): User {
        val encryptedPassword = passwordEncoder.encode(request.password.value)
        return User(
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            password = encryptedPassword,
            companyName = request.companyName,
            role = role
        )
    }

}