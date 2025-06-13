package com.coursy.auth.dto

data class JwtResponse(
    val token: String,
    val refreshToken: String,
)
