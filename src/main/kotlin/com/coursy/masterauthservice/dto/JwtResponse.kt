package com.coursy.masterauthservice.dto

// TODO primitive obsession
data class JwtResponse(
    val token: String,
//    val refreshToken: String,
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val companyName: String?,
    val role: String
)
