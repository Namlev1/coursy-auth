package com.coursy.auth.security

import arrow.core.Option
import arrow.core.getOrElse
import com.auth0.jwt.JWT
import com.coursy.auth.type.Email
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtTokenFilter : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        extractToken(request).map { token ->
            val jwt = JWT.decode(token)
            val email = Email.create(jwt.subject).getOrElse { failure ->
                log.warn("Invalid email format in JWT: ${jwt.subject}")
                response.status = 400
                response.writer.write(failure.message())
                return
            }

            val roleString = jwt.getClaim("role")?.asString()
            val authority = SimpleGrantedAuthority(roleString)

            val authentication = PreAuthenticatedAuthenticationToken(
                email,
                jwt,
                listOf(authority)
            )
            authentication.isAuthenticated = true
            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }

    private fun extractToken(request: HttpServletRequest): Option<String> {
        return Option.fromNullable(request.getHeader("Authorization"))
            .filter { it.startsWith("Bearer ") }
            .map { it.substring(7) }
    }
}
