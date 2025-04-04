package com.coursy.masterauthservice.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.masterauthservice.dto.JwtResponse
import com.coursy.masterauthservice.dto.LoginRequest
import com.coursy.masterauthservice.dto.RegistrationRequest
import com.coursy.masterauthservice.failure.RoleFailure
import com.coursy.masterauthservice.model.User
import com.coursy.masterauthservice.repository.RoleRepository
import com.coursy.masterauthservice.repository.UserRepository
import com.coursy.masterauthservice.security.JwtUtils
import com.coursy.masterauthservice.security.UserDetailsImp
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
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
    private val jwtUtils: JwtUtils
) {
    fun createUser(request: RegistrationRequest): Either<RoleFailure, Unit> {
        val role =
            roleRepository.findByName(request.roleName) ?: return RoleFailure
                .NotFound(request.roleName.toString())
                .left()
        val user = User(
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            companyName = request.companyName,
            role = role
        )
        userRepository.save(user)
        return Unit.right()
    }

    fun authenticateUser(loginRequest: LoginRequest): JwtResponse {
        val authentication: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(loginRequest.email, loginRequest.password)
        )

        SecurityContextHolder.getContext().authentication = authentication
        val jwt = jwtUtils.generateJwtToken(authentication)

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
            id = userDetails.id,
            email = userDetails.username,
            firstName = userDetails.firstName,
            lastName = userDetails.lastName,
            companyName = userDetails.companyName,
//            roles = roles
            role = role
        )
    }
}