package com.coursy.masterauthservice.dto

import com.coursy.masterauthservice.model.RoleName
import com.coursy.masterauthservice.type.Email

// todo primitive obsession
data class RegistrationRequest(
    val firstName: String,
    val lastName: String,
    val email: Email,
    val password: String,
    val companyName: String?,
    val roleName: RoleName
)