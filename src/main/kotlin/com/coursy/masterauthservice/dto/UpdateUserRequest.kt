package com.coursy.masterauthservice.dto

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.masterauthservice.failure.Failure
import com.coursy.masterauthservice.failure.RoleFailure
import com.coursy.masterauthservice.model.RoleName
import com.coursy.masterauthservice.type.CompanyName
import com.coursy.masterauthservice.type.Name

data class UserUpdateRequest(
    val firstName: String?,
    val lastName: String?,
    val companyName: String?,
    val roleName: String?
) {
    data class Validated(
        val firstName: Name?,
        val lastName: Name?,
        val companyName: CompanyName?,
        val roleName: RoleName?
    )

    fun validate(): Either<Failure, Validated> {
        val firstNameResult = firstName?.let { Name.create(it) }
        val lastNameResult = lastName?.let { Name.create(it) }
        val companyNameResult = companyName?.let { CompanyName.create(it) }
        val roleNameResult = roleName?.let { name ->
            Either.catch { RoleName.valueOf(name) }
                .mapLeft { RoleFailure.NotFound }
        }

        val firstError = listOfNotNull(
            firstNameResult?.leftOrNull(),
            lastNameResult?.leftOrNull(),
            companyNameResult?.leftOrNull(),
            roleNameResult?.leftOrNull()
        ).firstOrNull()

        return firstError?.left() ?: Validated(
            firstName = firstNameResult?.getOrNull(),
            lastName = lastNameResult?.getOrNull(),
            companyName = companyNameResult?.getOrNull(),
            roleName = roleNameResult?.getOrNull()
        ).right()
    }
}