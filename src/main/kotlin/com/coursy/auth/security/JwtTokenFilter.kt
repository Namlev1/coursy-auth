package com.coursy.auth.security

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.auth.failure.AuthHeaderFailure
import com.coursy.auth.failure.JwtFailure
import com.coursy.auth.jwt.JwtTokenService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtTokenFilter(
    private val jwtTokenService: JwtTokenService,
    private val userDetailsService: UserDetailsServiceImp
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            extractAndProcessJwt(request)
        } finally {
            filterChain.doFilter(request, response)
        }
    }

    private fun extractAndProcessJwt(request: HttpServletRequest) {
        parseJwt(request).fold(
            { authHeaderFailure -> handleHeaderFailure(authHeaderFailure) },
            { jwt -> authenticateWithJwt(jwt, request) }
        )
    }

    private fun handleHeaderFailure(error: AuthHeaderFailure) {
        when (error) {
            is AuthHeaderFailure.InvalidHeaderFormat ->
                logger.warn("Invalid Authorization header format: ${error.message()}")

            else -> {}
        }
    }

    private fun authenticateWithJwt(jwt: String, request: HttpServletRequest) {
        setAuthenticationContext(jwt, request).fold(
            { jwtError -> logger.warn("JWT token processing error: ${jwtError.message()}") },
            { /* Authentication successful, no action needed */ }
        )
    }

    private fun parseJwt(request: HttpServletRequest): Either<AuthHeaderFailure, String> {
        val headerAuth = request.getHeader("Authorization")
            ?: return AuthHeaderFailure.MissingHeader.left()

        return if (headerAuth.startsWith("Bearer ")) {
            headerAuth.removePrefix("Bearer ").trim().right()
        } else {
            AuthHeaderFailure.InvalidHeaderFormat(headerAuth).left()
        }
    }

    private fun setAuthenticationContext(
        jwt: String,
        request: HttpServletRequest
    ): Either<JwtFailure, Unit> {
        return try {
            val email = jwtTokenService.getUserEmailFromJwtToken(jwt)
            val userDetails = userDetailsService.loadUserByUsername(email)

            val authentication = UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.authorities
            ).apply {
                details = WebAuthenticationDetailsSource().buildDetails(request)
            }

            SecurityContextHolder.getContext().authentication = authentication
            Unit.right()
        } catch (e: Exception) {
            JwtFailure.InvalidToken(jwt).left()
        }
    }
}