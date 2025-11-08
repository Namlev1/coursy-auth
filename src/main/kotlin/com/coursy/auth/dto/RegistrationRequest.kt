package com.coursy.auth.dto

import com.coursy.auth.type.Email
import com.coursy.auth.type.Password
import java.util.*

data class RegistrationRequest(
    val email: Email,
    val password: Password,
    val id: UUID,
    val platformId: UUID?
)