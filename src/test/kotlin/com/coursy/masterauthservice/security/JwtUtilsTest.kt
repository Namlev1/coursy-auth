package com.coursy.masterauthservice.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.coursy.masterauthservice.type.Email
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority

class JwtUtilsTest : DescribeSpec({
    val jwtSecret = "testJwtSecret"
    val jwtExpirationMs = 2000 // 2 seconds
    val jwtUtils = JwtUtils(jwtSecret, jwtExpirationMs)

    val mockAuthentication = mockk<Authentication>()
    val mockUserDetails = mockk<UserDetailsImp>()
    val userEmail = Email.create("test@gmail.com").getOrNull()!!
    val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))

    beforeTest {
        every { mockUserDetails.authorities } returns authorities
        every { mockUserDetails.email } returns userEmail
        every { mockAuthentication.principal } returns mockUserDetails
    }

    afterTest {
        unmockkAll()
    }

    describe("generateJwtToken") {
        it("should generate valid JWT token with correct claims") {
            // when
            val token = jwtUtils.generateJwtToken(mockAuthentication)

            // then
            val decodedJWT = JWT.require(Algorithm.HMAC256(jwtSecret))
                .build()
                .verify(token)

            decodedJWT.subject shouldBe userEmail.value
            decodedJWT.getClaim("roles").asList(String::class.java) shouldBe authorities.map { it.authority }
        }
    }

    describe("getUserEmailFromJwtToken") {
        it("should extract user email from valid token") {
            // given
            val token = jwtUtils.generateJwtToken(mockAuthentication)

            // when
            val extractedUserId = jwtUtils.getUserEmailFromJwtToken(token)

            // then
            extractedUserId shouldBe userEmail.value
        }
    }

    describe("validateJwtToken") {
        it("should return true for valid token") {
            // given
            val token = jwtUtils.generateJwtToken(mockAuthentication)

            // when
            val isValid = jwtUtils.validateJwtToken(token)

            // then
            isValid shouldBe true
        }

        it("should return false for expired token") {
            // given
            val expiredToken = jwtUtils.generateJwtToken(mockAuthentication)

            // when
            Thread.sleep(3000)
            val isValid = jwtUtils.validateJwtToken(expiredToken)

            // then
            isValid shouldBe false
        }

        it("should return false for token with invalid signature") {
            // given
            val token = jwtUtils.generateJwtToken(mockAuthentication)

            // when - create a new JwtUtils with different secret to test validation
            val jwtUtilsWithDifferentSecret = JwtUtils("differentSecret", jwtExpirationMs)
            val isValid = jwtUtilsWithDifferentSecret.validateJwtToken(token)

            // then
            isValid shouldBe false
        }

        it("should return false for malformed token") {
            // given
            val malformedToken = "malformed.token.string"

            // when
            val isValid = jwtUtils.validateJwtToken(malformedToken)

            // then
            isValid shouldBe false
        }
    }
})