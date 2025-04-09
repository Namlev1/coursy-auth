package com.coursy.masterauthservice.jwt

import org.springframework.security.core.Authentication

interface JwtTokenService {
    fun generateJwtToken(authentication: Authentication): String
    fun getUserEmailFromJwtToken(token: String): String
    fun validateJwtToken(authToken: String): Boolean
}
