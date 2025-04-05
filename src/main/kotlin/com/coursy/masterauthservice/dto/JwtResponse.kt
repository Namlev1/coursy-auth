package com.coursy.masterauthservice.dto

import com.coursy.masterauthservice.type.Email

// TODO primitive obsession
data class JwtResponse(
    val token: String,
//    val refreshToken: String,
    val id: Long,
    val email: Email,
    val firstName: String,
    val lastName: String,
    val companyName: String?,
    val role: String
)
