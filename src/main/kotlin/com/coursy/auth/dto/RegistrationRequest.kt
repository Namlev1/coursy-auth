package com.coursy.auth.dto

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.auth.failure.Failure
import com.coursy.auth.failure.RoleFailure
import com.coursy.auth.model.RoleName
import com.coursy.auth.type.CompanyName
import com.coursy.auth.type.Email
import com.coursy.auth.type.Name
import com.coursy.auth.type.Password

data class RegistrationRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val companyName: String?,
    val roleName: String?
) : SelfValidating<Failure, RegistrationRequest.Validated> {
    data class Validated(
        val firstName: Name,
        val lastName: Name,
        val email: Email,
        val password: Password,
        val companyName: CompanyName?,
        val roleName: RoleName
    )

    override fun validate(): Either<Failure, Validated> {
        val firstNameResult = Name.create(firstName)
        val lastNameResult = Name.create(lastName)
        val emailResult = Email.create(email)
        val passwordResult = Password.create(password)
        val companyNameResult = companyName?.let { CompanyName.create(it) }
        val roleNameResult = roleName?.let {
            Either.catch { RoleName.valueOf(it) }
                .mapLeft { RoleFailure.NotFound }
        } ?: RoleName.ROLE_USER.right()

        val firstError = listOfNotNull(
            firstNameResult.leftOrNull(),
            lastNameResult.leftOrNull(),
            emailResult.leftOrNull(),
            passwordResult.leftOrNull(),
            companyNameResult?.leftOrNull(),
            roleNameResult.leftOrNull()
        ).firstOrNull()

        return firstError?.left() ?: Validated(
            firstName = firstNameResult.getOrNull()!!,
            lastName = lastNameResult.getOrNull()!!,
            email = emailResult.getOrNull()!!,
            password = passwordResult.getOrNull()!!,
            companyName = companyNameResult?.getOrNull(),
            roleName = roleNameResult.getOrNull()!!
        ).right()
    }
}