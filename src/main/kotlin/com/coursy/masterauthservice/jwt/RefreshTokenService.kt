package com.coursy.masterauthservice.jwt

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.masterauthservice.failure.UserFailure
import com.coursy.masterauthservice.model.RefreshToken
import com.coursy.masterauthservice.repository.RefreshTokenRepository
import com.coursy.masterauthservice.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userRepository: UserRepository,
    @Value("\${jwt.refresh-token-expiration}")
    private val refreshTokenDurationMs: Long
) {
    fun createRefreshToken(userId: Long): Either<UserFailure, RefreshToken> {
        val user = userRepository.findById(userId).getOrNull()
            ?: return UserFailure.IdNotExists.left()

        refreshTokenRepository.deleteByUser(user)

        val refreshToken = RefreshToken(
            user = user,
            token = UUID.randomUUID().toString(),
            expiryDate = Instant.now().plusMillis(refreshTokenDurationMs)
        )

        return refreshTokenRepository.save(refreshToken).right()
    }
}