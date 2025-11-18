/*
package com.coursy.auth.service

import arrow.core.left
import arrow.core.right
import com.coursy.auth.dto.LoginRequest
import com.coursy.auth.dto.RefreshJwtRequest
import com.coursy.auth.failure.AuthenticationFailure
import com.coursy.auth.failure.RefreshTokenFailure
import com.coursy.auth.jwt.JwtTokenService
import com.coursy.auth.jwt.RefreshTokenService
import com.coursy.auth.model.RefreshToken
import com.coursy.auth.model.User
import com.coursy.auth.repository.UserRepository
import com.coursy.auth.security.UserDetailsImp
import com.coursy.auth.type.Email
import com.coursy.auth.type.Password
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class AuthServiceTest : DescribeSpec({

    beforeTest {
        clearAllMocks()
    }

    afterTest {
        unmockkAll()
    }

    class TestFixtures {
        // Common values
        val userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        val jwtToken = "test.jwt.token"
        val refreshToken = "test-refresh-token"

        // User data
        val email = Email.create("test@example.com").getOrNull()!!
        val password = Password.create("Password123!").getOrNull()!!

        // Request objects
        val validLoginRequest by lazy {
            LoginRequest.Validated(
                email = email,
                password = password
            )
        }

        val validRefreshRequest by lazy {
            RefreshJwtRequest.Validated(
                refreshToken = refreshToken
            )
        }

        // User object
        fun createUser(
            id: UUID = userId,
            email: Email = this.email,
            password: String = "encodedPassword",
            lastLogin: Instant = Instant.now(),
            enabled: Boolean = true,
            accountNonLocked: Boolean = true,
            failedAttempts: Int = 0,
        ) = User(
            id = id,
            email = email,
            password = password,
            lastLogin = lastLogin,
            enabled = enabled,
            accountNonLocked = accountNonLocked,
            failedAttempts = failedAttempts
        )

        // RefreshToken object
        fun createRefreshToken(
            id: UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001"),
            token: String = refreshToken,
            user: User = createUser(),
            expiryDate: Instant = Instant.now().plus(7, ChronoUnit.DAYS),
        ) = RefreshToken(
            id = id,
            token = token,
            user = user,
            expiryDate = expiryDate
        )

        // UserDetails object
        fun createUserDetails(
            id: UUID = userId,
            email: Email = this.email,
            password: String = this.password.value,
            enabled: Boolean = true,
            accountNonLocked: Boolean = true,
        ) = UserDetailsImp(
            id = id,
            email = email,
            password = password,
            enabled = enabled,
            accountNonLocked = accountNonLocked
        )
    }

    // Mocks
    val userRepository = mockk<UserRepository>()
    val authenticationManager = mockk<AuthenticationManager>()
    val jwtTokenService = mockk<JwtTokenService>()
    val refreshTokenService = mockk<RefreshTokenService>()
    val authentication = mockk<Authentication>()

    // System under test
    val authService = AuthService(userRepository, authenticationManager, jwtTokenService, refreshTokenService)

    val fixtures = TestFixtures()

    describe("AuthService") {

        describe("User Authentication") {
            context("when authenticating with valid credentials") {
                it("should authenticate user successfully") {
                    // given
                    val request = fixtures.validLoginRequest
                    val userDetails = fixtures.createUserDetails()
                    val user = fixtures.createUser()

                    every { authenticationManager.authenticate(any()) } returns authentication
                    every { authentication.principal } returns userDetails
                    every { jwtTokenService.generateJwtToken(authentication) } returns fixtures.jwtToken
                    every { refreshTokenService.createRefreshToken(fixtures.userId) } returns fixtures.createRefreshToken()
                        .right()
                    every { userRepository.findById(fixtures.userId) } returns Optional.of(user)
                    every { userRepository.save(any()) } returns user

                    // when
                    val result = authService.authenticateUser(request)

                    // then
                    val response = result.shouldBeRight()
                    response.token shouldBe fixtures.jwtToken
                    response.refreshToken shouldBe fixtures.refreshToken

                    verify {
                        authenticationManager.authenticate(
                            UsernamePasswordAuthenticationToken(
                                request.email.value,
                                request.password.value
                            )
                        )
                    }
                    verify { jwtTokenService.generateJwtToken(authentication) }
                    verify { refreshTokenService.createRefreshToken(fixtures.userId) }
                    verify { userRepository.findById(fixtures.userId) }

                    val userSlot = slot<User>()
                    verify { userRepository.save(capture(userSlot)) }
                    userSlot.captured.failedAttempts shouldBe 0
                    userSlot.captured.lastLogin shouldBe userSlot.captured.lastLogin
                }
            }

            context("when authentication fails") {
                it("should return InvalidCredentials failure") {
                    // given
                    val request = fixtures.validLoginRequest

                    every { authenticationManager.authenticate(any()) } throws RuntimeException("Authentication failed")

                    // when
                    val result = authService.authenticateUser(request)

                    // then
                    result.shouldBeLeft().shouldBeInstanceOf<AuthenticationFailure.InvalidCredentials>()
                    verify {
                        authenticationManager.authenticate(
                            UsernamePasswordAuthenticationToken(
                                request.email.value,
                                request.password.value
                            )
                        )
                    }
                    verify(exactly = 0) { refreshTokenService.createRefreshToken(any()) }
                    verify(exactly = 0) { userRepository.findById(any()) }
                    verify(exactly = 0) { userRepository.save(any()) }
                }
            }

        }

        describe("JWT Token Refresh") {
            context("when refreshing with valid token") {
                it("should refresh token successfully") {
                    // given
                    val request = fixtures.validRefreshRequest
                    val refreshToken = fixtures.createRefreshToken()
                    fixtures.createUserDetails()

                    every { refreshTokenService.findByToken(fixtures.refreshToken) } returns refreshToken.right()
                    every { refreshTokenService.verifyExpiration(refreshToken) } returns refreshToken.right()
                    every { jwtTokenService.generateJwtToken(any<UserDetailsImp>()) } returns fixtures.jwtToken

                    // when
                    val result = authService.refreshJwtToken(request)

                    // then
                    val response = result.shouldBeRight()
                    response.token shouldBe fixtures.jwtToken
                    response.refreshToken shouldBe fixtures.refreshToken

                    verify { refreshTokenService.findByToken(fixtures.refreshToken) }
                    verify { refreshTokenService.verifyExpiration(refreshToken) }
                    verify { jwtTokenService.generateJwtToken(any<UserDetailsImp>()) }
                }
            }

            context("when token not found") {
                it("should return NotFound failure") {
                    // given
                    val request = fixtures.validRefreshRequest

                    every { refreshTokenService.findByToken(fixtures.refreshToken) } returns RefreshTokenFailure.NotFound.left()

                    // when
                    val result = authService.refreshJwtToken(request)

                    // then
                    result.shouldBeLeft().shouldBeInstanceOf<RefreshTokenFailure.NotFound>()
                    verify { refreshTokenService.findByToken(fixtures.refreshToken) }
                    verify(exactly = 0) { refreshTokenService.verifyExpiration(any()) }
                    verify(exactly = 0) { jwtTokenService.generateJwtToken(any<UserDetailsImp>()) }
                }
            }

            context("when token is expired") {
                it("should return TokenExpired failure") {
                    // given
                    val request = fixtures.validRefreshRequest
                    val refreshToken = fixtures.createRefreshToken()

                    every { refreshTokenService.findByToken(fixtures.refreshToken) } returns refreshToken.right()
                    every { refreshTokenService.verifyExpiration(refreshToken) } returns RefreshTokenFailure.Expired.left()

                    // when
                    val result = authService.refreshJwtToken(request)

                    // then
                    result.shouldBeLeft().shouldBeInstanceOf<RefreshTokenFailure.Expired>()
                    verify { refreshTokenService.findByToken(fixtures.refreshToken) }
                    verify { refreshTokenService.verifyExpiration(refreshToken) }
                    verify(exactly = 0) { jwtTokenService.generateJwtToken(any<UserDetailsImp>()) }
                }
            }
        }
    }
})*/
