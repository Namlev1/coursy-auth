package com.coursy.masterauthservice.security

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.masterauthservice.jwt.JwtTokenService
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

    // TODO check everything if working correctly. Refactor later.
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        parseJwt(request).fold(
            { error ->
                logger.error("JWT parse error: $error")
            },
            { jwt ->
                if (jwtTokenService.validateJwtToken(jwt)) {
                    val email = jwtTokenService.getUserEmailFromJwtToken(jwt)
                    val userDetails = userDetailsService.loadUserByUsername(email)
                    val authentication = UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.authorities
                    )
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        )

        filterChain.doFilter(request, response)
    }

    // todo add failures
    private fun parseJwt(request: HttpServletRequest): Either<String, String> {
        val headerAuth = request.getHeader("Authorization")
            ?: return "Missing Authorization header".left()

        return if (headerAuth.startsWith("Bearer ")) {
            headerAuth.removePrefix("Bearer ").right()
        } else {
            "Invalid Authorization header format".left()
        }
    }
}