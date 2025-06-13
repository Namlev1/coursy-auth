package com.coursy.auth.jwt

import com.coursy.auth.security.UserDetailsImp
import org.springframework.security.core.Authentication

interface JwtTokenService {
    fun generateJwtToken(authentication: Authentication): String
    fun generateJwtToken(userDetailsImp: UserDetailsImp): String
    fun getUserEmailFromJwtToken(token: String): String
    fun isJwtTokenValid(authToken: String): Boolean
}
