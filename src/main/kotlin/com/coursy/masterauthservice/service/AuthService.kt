package com.coursy.masterauthservice.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.masterauthservice.dto.JwtResponse
import com.coursy.masterauthservice.dto.LoginRequest
import com.coursy.masterauthservice.dto.RefreshJwtRequest
import com.coursy.masterauthservice.failure.AuthenticationFailure
import com.coursy.masterauthservice.failure.Failure
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

        // Update last login
        val user = userRepository.findById(userDetails.id).get()
        user.lastLogin = Instant.now()
        user.failedAttempts = 0
        userRepository.save(user)

        return JwtResponse(
            token = jwt,
            refreshToken = refreshToken
        ).right()
    }

    // TODO Refactor this method
    //  Please correct failure type if applicable.
    fun refreshJwtToken(refreshRequest: RefreshJwtRequest.Validated): Either<Failure, JwtResponse> {
        val refreshToken = refreshTokenService.findByToken(refreshRequest.refreshToken)
            .fold(
                { return it.left() },
                { token -> token }
            )

        refreshTokenService.verifyExpiration(refreshToken)
            .onLeft { return it.left() }

        val userDetailsImp = refreshToken.user.toUserDetails()
        val jwt = jwtTokenService.generateJwtToken(userDetailsImp)

        return JwtResponse(
            token = jwt,
            refreshToken = refreshToken.token
        ).right()
    }
}