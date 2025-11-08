package com.coursy.auth.dto

import java.util.*

data class OwnerRegistrationRequest(
    val currentUserId: UUID,
    val newUserId: UUID,
    val platformId: UUID
)
