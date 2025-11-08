package com.coursy.auth.service

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.coursy.auth.dto.*
import com.coursy.auth.failure.AuthenticationFailure
import com.coursy.auth.failure.Failure
import com.coursy.auth.failure.RefreshTokenFailure
import com.coursy.auth.failure.UserFailure
import com.coursy.auth.jwt.JwtTokenService
import com.coursy.auth.jwt.RefreshTokenService
import com.coursy.auth.model.User
import com.coursy.auth.repository.UserRepository
import com.coursy.auth.security.UserDetailsImp
import com.coursy.auth.security.toUserDetails
import jakarta.transaction.Transactional
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenService: JwtTokenService,
    private val refreshTokenService: RefreshTokenService,
    private val passwordEncoder: PasswordEncoder
) {
    fun authenticateUser(loginRequest: LoginRequest.Validated): Either<Failure, JwtResponse> {
        val platformIdPart = loginRequest.platformId?.toString() ?: "HOST_PLATFORM"
        val compositeUsername = "${loginRequest.email.value}::${platformIdPart}"
        
        val authentication = runCatching {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(compositeUsername, loginRequest.password.value)
            )
        }.getOrElse { return AuthenticationFailure.InvalidCredentials.left() }

        SecurityContextHolder.getContext().authentication = authentication

        val jwt = jwtTokenService.generateJwtToken(authentication.principal as UserDetailsImp)

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

    fun registerUser(request: RegistrationRequest): Either<UserFailure, Unit> {
        val encryptedPassword = passwordEncoder.encode(request.password.value)
        val user = User(
            id = request.id,
            email = request.email,
            password = encryptedPassword,
            platformId = request.platformId
        )

        try {
            userRepository.save(user)
            return Unit.right()
        } catch (e: Exception) {
            return UserFailure.IdExists.left()
        }
    }

    fun createOwner(request: OwnerRegistrationRequest): Either<UserFailure, Unit> {
        val ownerOriginal = userRepository.findById(request.currentUserId)
            .orElseThrow()
        val ownerAccount = User(
            id = request.newUserId,
            email = ownerOriginal.email,
            password = ownerOriginal.password,
            platformId = request.platformId
        )

        userRepository.save(ownerAccount)
        return Unit.right()
    }

    fun logoutUser(id: UUID): Either<Failure, Unit> {
        val user = userRepository.findById(id)
            .orElseThrow()

        refreshTokenService.invalidateUserTokens(user)

        return Unit.right()
    }

    private fun updateLastLogin(userDetails: UserDetailsImp) {
        val user = userRepository.findById(userDetails.id).get()
        user.lastLogin = Instant.now()
        user.failedAttempts = 0
        userRepository.save(user)
    }
}