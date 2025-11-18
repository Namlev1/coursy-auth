package com.coursy.auth.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.coursy.auth.internal.users.UserResponse
import com.coursy.auth.internal.users.UsersServiceClient
import com.coursy.auth.security.UserDetailsImp
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtUtils(
    @Value("\${jwt.secret}")
    private val jwtSecret: String,
    @Value("\${jwt.access-token-expiration:604800000}")
    private val jwtExpirationMs: Int,
    private val usersServiceClient: UsersServiceClient
) : JwtTokenService {
    private val algorithm = Algorithm.HMAC256(jwtSecret)
    private val verifier = JWT.require(algorithm).build()

    override fun generateJwtToken(userDetailsImp: UserDetailsImp): String =
        generateJwt(userDetailsImp)
    
    override fun getUserEmailFromJwtToken(token: String): String =
        verifier
            .verify(token)
            .subject

    override fun isJwtTokenValid(authToken: String): Boolean =
        runCatching {
            verifier.verify(authToken)
            true
        }.getOrElse { false }

    private fun generateJwt(userDetails: UserDetailsImp): String {
        val userData = usersServiceClient.getUser(userDetails.id)
        return generateJwt(userData)
    }

    private fun generateJwt(
        userData: UserResponse
    ): String = userData.run {
        JWT.create()
            .withSubject(email)
            .withClaim("id", id.toString())
            .withClaim("platformId", platformId?.toString())
            .withClaim("role", roleName.toString())
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + jwtExpirationMs))
            .sign(Algorithm.HMAC256(jwtSecret))
    }
}