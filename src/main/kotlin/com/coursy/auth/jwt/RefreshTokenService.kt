package com.coursy.auth.jwt

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.auth.failure.RefreshTokenFailure
import com.coursy.auth.failure.UserFailure
import com.coursy.auth.model.RefreshToken
import com.coursy.auth.model.User
import com.coursy.auth.repository.RefreshTokenRepository
import com.coursy.auth.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userRepository: UserRepository,
    @Value("\${jwt.refresh-token-expiration}")
    private val refreshTokenDurationMs: Long
) {
    fun createRefreshToken(userId: UUID): Either<UserFailure, RefreshToken> {
        val user = userRepository.findById(userId).getOrNull()
            ?: return UserFailure.IdNotExists.left()

        refreshTokenRepository.deleteByUser(user)
        refreshTokenRepository.flush()

        val refreshToken = RefreshToken(
            user = user,
            token = UUID.randomUUID().toString(),
            expiryDate = Instant.now().plusMillis(refreshTokenDurationMs)
        )

        return refreshTokenRepository.save(refreshToken).right()
    }

    fun findByToken(token: String) =
        refreshTokenRepository.findByToken(token)
            ?.right() ?: RefreshTokenFailure.NotFound.left()

    fun verifyExpiration(token: RefreshToken): Either<RefreshTokenFailure, RefreshToken> {
        return if (token.expiryDate.isBefore(Instant.now())) {
            refreshTokenRepository.delete(token)
            RefreshTokenFailure.Expired.left()
        } else {
            token.right()
        }
    }

    fun invalidateUserTokens(user: User) {
        refreshTokenRepository.deleteByUser(user)
    }
}