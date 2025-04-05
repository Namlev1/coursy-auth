package com.coursy.masterauthservice.dto

import com.coursy.masterauthservice.model.RoleName
import com.coursy.masterauthservice.type.Email
import com.coursy.masterauthservice.type.Name

// todo primitive obsession
data class RegistrationRequest(
    val firstName: Name,
    val lastName: Name,
    val email: Email,
    val password: String,
    val companyName: String?,
    val roleName: RoleName
)