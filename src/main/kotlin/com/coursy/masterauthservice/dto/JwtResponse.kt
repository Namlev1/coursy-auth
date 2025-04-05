package com.coursy.masterauthservice.dto

import com.coursy.masterauthservice.type.Email
import com.coursy.masterauthservice.type.Name

// TODO primitive obsession
data class JwtResponse(
    val token: String,
//    val refreshToken: String,
    val id: Long,
    val email: Email,
    val firstName: Name,
    val lastName: Name,
    val companyName: String?,
    val role: String
)
