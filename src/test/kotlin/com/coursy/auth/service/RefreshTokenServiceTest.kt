package com.coursy.auth.service

import com.coursy.auth.failure.UserFailure
import com.coursy.auth.jwt.RefreshTokenService
import com.coursy.auth.model.RefreshToken
import com.coursy.auth.model.User
import com.coursy.auth.repository.RefreshTokenRepository
import com.coursy.auth.repository.UserRepository
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import java.util.*
import kotlin.jvm.optionals.getOrNull

class RefreshTokenServiceTest : DescribeSpec({
    val refreshTokenDurationMs = 86400000L // 24 hours
    val refreshTokenRepository = mockk<RefreshTokenRepository>()
    val userRepository = mockk<UserRepository>()

    val refreshTokenService = RefreshTokenService(
        refreshTokenRepository = refreshTokenRepository,
        userRepository = userRepository,
        refreshTokenDurationMs = refreshTokenDurationMs
    )

    beforeTest {
        clearAllMocks()
    }

    afterTest {
        unmockkAll()
    }

    describe("createRefreshToken") {
        context("when creating new refresh token") {
            it("should create and return a valid refresh token for existing user") {
                // given
                val userId = 1L
                val mockUser = mockk<User>()
                val mockRefreshToken = mockk<RefreshToken>()
                val mockOptional = mockk<Optional<User>>()

                every { mockOptional.getOrNull() } returns mockUser
                every { userRepository.findById(userId) } returns mockOptional
                every { refreshTokenRepository.flush() } just runs
                every { refreshTokenRepository.deleteByUser(mockUser) } just runs
                every {
                    refreshTokenRepository.save(any())
                } answers {
                    firstArg()
                }

                every { refreshTokenRepository.save(any()) } returns mockRefreshToken

                // when
                val result = refreshTokenService.createRefreshToken(userId)

                // then
                val token = result.shouldBeRight()
                token shouldBe mockRefreshToken

                verify(exactly = 1) { userRepository.findById(userId) }
                verify(exactly = 1) { refreshTokenRepository.deleteByUser(mockUser) }
                verify(exactly = 1) { refreshTokenRepository.save(any()) }
            }

            it("should return UserFailure.IdNotExists when user not found") {
                // given
                val userId = 999L
                val mockOptional = mockk<Optional<User>>()

                every { mockOptional.getOrNull() } returns null
                every { userRepository.findById(userId) } returns mockOptional

                // when
                val result = refreshTokenService.createRefreshToken(userId)

                // then
                result.shouldBeLeft()
                    .shouldBeInstanceOf<UserFailure.IdNotExists>()

                verify(exactly = 1) { userRepository.findById(userId) }
                verify(exactly = 0) { refreshTokenRepository.deleteByUser(any()) }
                verify(exactly = 0) { refreshTokenRepository.save(any()) }
            }
        }
    }
})