package com.coursy.masterauthservice.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtUtils {
    @Value("\${jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${jwt.access-token-expiration}")
    private var jwtExpirationMs: Int = 0

    fun generateJwtToken(authentication: Authentication): String =
        (authentication.principal as UserDetailsImp).let { generateJwt(it.email.value, it.authorities) }

    fun getUserNameFromJwtToken(token: String): String =
        JWT.require(Algorithm.HMAC256(jwtSecret))
            .build()
            .verify(token)
            .subject

    fun validateJwtToken(authToken: String): Boolean =
        runCatching {
            JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(authToken)
            true
        }.getOrElse { false }

    private fun generateJwt(subject: String, roles: Collection<GrantedAuthority>): String = JWT.create()
        .withSubject(subject)
        .withClaim("roles", roles.map { it.authority })
        .withIssuedAt(Date())
        .withExpiresAt(Date(System.currentTimeMillis() + jwtExpirationMs))
        .sign(Algorithm.HMAC256(jwtSecret))
}