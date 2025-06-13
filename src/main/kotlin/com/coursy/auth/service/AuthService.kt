package com.coursy.auth.service

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.coursy.auth.dto.JwtResponse
import com.coursy.auth.dto.LoginRequest
import com.coursy.auth.dto.RefreshJwtRequest
import com.coursy.auth.failure.AuthenticationFailure
import com.coursy.auth.failure.Failure
import com.coursy.auth.failure.RefreshTokenFailure
import com.coursy.auth.jwt.JwtTokenService
import com.coursy.auth.jwt.RefreshTokenService
import com.coursy.auth.repository.UserRepository
import com.coursy.auth.security.UserDetailsImp
import com.coursy.auth.security.toUserDetails
import jakarta.transaction.Transactional
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.time.Instant

@Service
@Transactional
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