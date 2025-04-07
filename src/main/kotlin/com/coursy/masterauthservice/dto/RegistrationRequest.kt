package com.coursy.masterauthservice.dto

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.coursy.masterauthservice.failure.Failure
import com.coursy.masterauthservice.failure.RoleFailure
import com.coursy.masterauthservice.model.RoleName
import com.coursy.masterauthservice.type.CompanyName
import com.coursy.masterauthservice.type.Email
import com.coursy.masterauthservice.type.Name
import com.coursy.masterauthservice.type.Password

data class RegistrationRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val companyName: String?,
    val roleName: String
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
        val firstName = Name.create(firstName).getOrElse { return it.left() }
        val lastName = Name.create(lastName).getOrElse { return it.left() }
        val email = Email.create(email).getOrElse { return it.left() }
        val password = Password.create(password).getOrElse { return it.left() }
        val companyName = companyName?.let {
            CompanyName.create(it).getOrElse { failure -> return failure.left() }
        }
        val roleName = try {
            RoleName.valueOf(roleName)
        } catch (e: IllegalArgumentException) {
            return RoleFailure.NotFound.left()
        }
        return Validated(
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password,
            companyName = companyName,
            roleName = roleName
        ).right()
    }
}