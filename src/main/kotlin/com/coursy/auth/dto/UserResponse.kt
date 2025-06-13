package com.coursy.auth.dto

import com.coursy.auth.model.User
import com.coursy.auth.type.CompanyName
import com.coursy.auth.type.Email
import com.coursy.auth.type.Name

data class UserResponse(
    val id: Long,
    val email: Email,
    val firstName: Name,
    val lastName: Name,
    val companyName: CompanyName?,
    val roleName: String,
)

fun User.toUserResponse(): UserResponse {
    return UserResponse(
        id = this.id,
        email = this.email,
        firstName = this.firstName,
        lastName = this.lastName,
        companyName = this.companyName,
        roleName = this.role.name.name
    )
} 