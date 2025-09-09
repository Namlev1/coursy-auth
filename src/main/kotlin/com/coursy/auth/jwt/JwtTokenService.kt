package com.coursy.auth.jwt

import com.coursy.auth.security.UserDetailsImp

interface JwtTokenService {
    fun generateJwtToken(userDetailsImp: UserDetailsImp): String
    fun getUserEmailFromJwtToken(token: String): String
    fun isJwtTokenValid(authToken: String): Boolean
}
