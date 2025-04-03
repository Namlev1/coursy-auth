package com.coursy.masterauthservice.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtUtils {

    @Value("\${jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${jwt.access-token-expiration}")
    private var jwtExpirationMs: Int = 0

    fun generateJwtToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal as UserDetailsImp

        return generateJwt(userPrincipal.username)
    }

    // TODO needed?
    fun getUserNameFromJwtToken(token: String): String {
        return JWT.require(Algorithm.HMAC256(jwtSecret))
            .build()
            .verify(token)
            .subject
    }

    fun validateJwtToken(authToken: String): Boolean {
        try {
            JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(authToken)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun generateJwt(subject: String) = JWT.create()
        .withSubject(subject)
        .withIssuedAt(Date())
        .withExpiresAt(Date(Date().time + jwtExpirationMs))
        .sign(Algorithm.HMAC256(jwtSecret))
}