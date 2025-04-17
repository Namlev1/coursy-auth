package com.coursy.masterauthservice.service

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.coursy.masterauthservice.dto.JwtResponse
import com.coursy.masterauthservice.dto.LoginRequest
import com.coursy.masterauthservice.dto.RefreshJwtRequest
import com.coursy.masterauthservice.failure.AuthenticationFailure
import com.coursy.masterauthservice.failure.Failure
import com.coursy.masterauthservice.failure.RefreshTokenFailure
import com.coursy.masterauthservice.jwt.JwtTokenService
import com.coursy.masterauthservice.jwt.RefreshTokenService
import com.coursy.masterauthservice.repository.UserRepository
import com.coursy.masterauthservice.security.UserDetailsImp
import com.coursy.masterauthservice.security.toUserDetails
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenService: JwtTokenService,
    private val refreshTokenService: RefreshTokenService
) {
    fun authenticateUser(loginRequest: LoginRequest.Validated): Either<Failure, JwtResponse> {
        val authentication = runCatching {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(loginRequest.email.value, loginRequest.password.value)
            )
        }.getOrElse { return AuthenticationFailure.InvalidCredentials.left() }

        SecurityContextHolder.getContext().authentication = authentication
        val jwt = jwtTokenService.generateJwtToken(authentication)

        val userDetails = authentication.principal as UserDetailsImp

        val refreshToken = refreshTokenService.createRefreshToken(userDetails.id)
            .fold(
                { failure -> return failure.left() },
                { token -> token.token }
            )

        updateLastLogin(userDetails)

        return JwtResponse(
            token = jwt,
            refreshToken = refreshToken
        ).right()
    }

    fun refreshJwtToken(refreshRequest: RefreshJwtRequest.Validated): Either<RefreshTokenFailure, JwtResponse> {
        val refreshToken = refreshTokenService.findByToken(refreshRequest.refreshToken)
            .getOrElse { failure -> return failure.left() }

        refreshTokenService.verifyExpiration(refreshToken)
            .onLeft { failure -> return failure.left() }

        val userDetails = refreshToken.user.toUserDetails()
        val newJwt = jwtTokenService.generateJwtToken(userDetails)

        return JwtResponse(
            token = newJwt,
            refreshToken = refreshToken.token
        ).right()
    }

    private fun updateLastLogin(userDetails: UserDetailsImp) {
        val user = userRepository.findById(userDetails.id).get()
        user.lastLogin = Instant.now()
        user.failedAttempts = 0
        userRepository.save(user)
    }
}