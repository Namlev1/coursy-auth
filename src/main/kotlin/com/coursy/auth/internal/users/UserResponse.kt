package com.coursy.auth.internal.users

import com.coursy.auth.model.Role
import java.util.*

data class UserResponse(
    val id: UUID,
    val platformId: UUID?,
    val email: String,
    val firstName: String,
    val lastName: String,
    val roleName: Role,
)
