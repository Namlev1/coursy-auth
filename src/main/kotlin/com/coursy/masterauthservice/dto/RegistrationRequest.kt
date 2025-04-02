package com.coursy.masterauthservice.dto

import com.coursy.masterauthservice.model.RoleName

// todo primitive obsession
data class RegistrationRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val companyName: String?,
    val roleName: RoleName
)