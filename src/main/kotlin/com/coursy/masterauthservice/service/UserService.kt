package com.coursy.masterauthservice.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.masterauthservice.dto.JwtResponse
import com.coursy.masterauthservice.dto.LoginRequest
import com.coursy.masterauthservice.dto.RegistrationRequest
import com.coursy.masterauthservice.failure.AuthenticationFailure
import com.coursy.masterauthservice.failure.Failure
import com.coursy.masterauthservice.failure.RoleFailure
import com.coursy.masterauthservice.jwt.JwtTokenService
import com.coursy.masterauthservice.model.User
import com.coursy.masterauthservice.repository.RoleRepository
import com.coursy.masterauthservice.repository.UserRepository
import com.coursy.masterauthservice.security.UserDetailsImp
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UserService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenService: JwtTokenService
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

    fun authenticateUser(loginRequest: LoginRequest.Validated): Either<AuthenticationFailure, JwtResponse> {
        val authentication = runCatching {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(loginRequest.email.value, loginRequest.password.value)
            )
        }.getOrElse { return AuthenticationFailure.InvalidCredentials.left() }

        SecurityContextHolder.getContext().authentication = authentication
        val jwt = jwtTokenService.generateJwtToken(authentication)

        val userDetails = authentication.principal as UserDetailsImp
        val role = userDetails.authorities.first().toString()

        // TODO
//        val refreshToken = refreshTokenService.createRefreshToken(userDetails.id)

        // Update last login
        val user = userRepository.findById(userDetails.id).get()
        user.lastLogin = Instant.now()
        user.failedAttempts = 0
        userRepository.save(user)

        return JwtResponse(
            token = jwt,
//            refreshToken = refreshToken.token,
        ).right()
    }
}