package com.coursy.auth.dto

import com.coursy.auth.failure.*
import com.coursy.auth.model.RoleName
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class RegistrationRequestTest : DescribeSpec({
    val validFirstName = "John"
    val validLastName = "Doe"
    val validEmail = "john.doe@example.com"
    val validPassword = "StrongPass123!"
    val validCompanyName = "Acme Inc"
    val validRoleName = "ROLE_ADMIN"

    describe("RegistrationRequest validation") {
        it("should validate successfully") {
            // given
            val request = RegistrationRequest(
                firstName = validFirstName,
                lastName = validLastName,
                email = validEmail,
                password = validPassword,
                companyName = validCompanyName,
                roleName = validRoleName
            )

            // when
            val result = request.validate()

            // then
            val right = result.shouldBeRight()
            right.firstName.value shouldBe validFirstName
            right.lastName.value shouldBe validLastName
            right.email.value shouldBe validEmail
            right.password.value shouldBe validPassword
            right.companyName?.value shouldBe validCompanyName
            right.roleName shouldBe RoleName.ROLE_ADMIN
        }
    }

    it("should validate successfully with null company name") {
        // given
        val request = RegistrationRequest(
            firstName = validFirstName,
            lastName = validLastName,
            email = validEmail,
            password = validPassword,
            companyName = null,
            roleName = validRoleName
        )

        // when
        val result = request.validate()

        // then
        val right = result.shouldBeRight()
        right.companyName shouldBe null
    }

    context("with invalid input") {
        it("should fail with invalid first name") {
            // given
            val request = RegistrationRequest(
                firstName = "",
                lastName = validLastName,
                email = validEmail,
                password = validPassword,
                companyName = validCompanyName,
                roleName = validRoleName
            )

            // when
            val result = request.validate()

            // then
            result.shouldBeLeft().shouldBeInstanceOf<NameFailure.Empty>()
        }

        it("should fail with invalid last name") {
            // given
            val request = RegistrationRequest(
                firstName = validFirstName,
                lastName = "",
                email = validEmail,
                password = validPassword,
                companyName = validCompanyName,
                roleName = validRoleName
            )

            // when
            val result = request.validate()

            // then
            result.shouldBeLeft().shouldBeInstanceOf<NameFailure.Empty>()
        }

        it("should fail with invalid email") {
            // given
            val request = RegistrationRequest(
                firstName = validFirstName,
                lastName = validLastName,
                email = "invalid-email",
                password = validPassword,
                companyName = validCompanyName,
                roleName = validRoleName
            )

            // when
            val result = request.validate()

            // then
            result.shouldBeLeft().shouldBeInstanceOf<EmailFailure.MissingAtSymbol>()
        }

        it("should fail with invalid password") {
            // given
            val request = RegistrationRequest(
                firstName = validFirstName,
                lastName = validLastName,
                email = validEmail,
                password = "weak",
                companyName = validCompanyName,
                roleName = validRoleName
            )

            // when
            val result = request.validate()

            // then
            result.shouldBeLeft().shouldBeInstanceOf<PasswordFailure>()
        }

        it("should fail with invalid company name") {
            // given
            val request = RegistrationRequest(
                firstName = validFirstName,
                lastName = validLastName,
                email = validEmail,
                password = validPassword,
                companyName = "", // Empty company name
                roleName = validRoleName
            )

            // when
            val result = request.validate()

            // then
            result.shouldBeLeft().shouldBeInstanceOf<CompanyNameFailure.Empty>()
        }

        it("should fail with invalid role name") {
            // given
            val request = RegistrationRequest(
                firstName = validFirstName,
                lastName = validLastName,
                email = validEmail,
                password = validPassword,
                companyName = validCompanyName,
                roleName = "INVALID_ROLE"
            )

            // when
            val result = request.validate()

            // then
            result.shouldBeLeft().shouldBeInstanceOf<RoleFailure.NotFound>()
        }
    }
})