package com.coursy.masterauthservice.dto

import com.coursy.masterauthservice.type.Email

// TODO primitive obsession
data class LoginRequest(
    val email: Email,
    val password: String
)
