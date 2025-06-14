package com.coursy.auth.dto

import com.coursy.auth.failure.CompanyNameFailure
import com.coursy.auth.failure.NameFailure
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class UserUpdateRequestTest : DescribeSpec({
    val validFirstName = "John"
    val validLastName = "Doe"
    val validCompanyName = "Acme Inc"
    val validRoleName = "ROLE_ADMIN"

    describe("UserUpdateRequest validation") {
        it("should validate successfully with all fields") {
            // given
            val request = UserUpdateRequest(
                firstName = validFirstName,
                lastName = validLastName,
                companyName = validCompanyName,
                roleName = validRoleName
            )

            // when
            val result = request.validate()

            // then
            val right = result.shouldBeRight()
            right.firstName?.value shouldBe validFirstName
            right.lastName?.value shouldBe validLastName
            right.companyName?.value shouldBe validCompanyName
            right.roleName shouldBe RoleName.ROLE_ADMIN
        }

        it("should validate successfully with only firstName") {
            // given
            val request = UserUpdateRequest(
                firstName = validFirstName,
                lastName = null,
                companyName = null,
                roleName = null
            )

            // when
            val result = request.validate()

            // then
            val right = result.shouldBeRight()
            right.firstName?.value shouldBe validFirstName
            right.lastName shouldBe null
            right.companyName shouldBe null
            right.roleName shouldBe null
        }

        it("should validate successfully with only lastName") {
            // given
            val request = UserUpdateRequest(
                firstName = null,
                lastName = validLastName,
                companyName = null,
                roleName = null
            )

            // when
            val result = request.validate()

            // then
            val right = result.shouldBeRight()
            right.firstName shouldBe null
            right.lastName?.value shouldBe validLastName
            right.companyName shouldBe null
            right.roleName shouldBe null
        }

        it("should validate successfully with only companyName") {
            // given
            val request = UserUpdateRequest(
                firstName = null,
                lastName = null,
                companyName = validCompanyName,
                roleName = null
            )

            // when
            val result = request.validate()

            // then
            val right = result.shouldBeRight()
            right.firstName shouldBe null
            right.lastName shouldBe null
            right.companyName?.value shouldBe validCompanyName
            right.roleName shouldBe null
        }

        it("should validate successfully with only roleName") {
            // given
            val request = UserUpdateRequest(
                firstName = null,
                lastName = null,
                companyName = null,
                roleName = validRoleName
            )

            // when
            val result = request.validate()

            // then
            val right = result.shouldBeRight()
            right.firstName shouldBe null
            right.lastName shouldBe null
            right.companyName shouldBe null
            right.roleName shouldBe RoleName.ROLE_ADMIN
        }

        it("should validate successfully with all fields null") {
            // given
            val request = UserUpdateRequest(
                firstName = null,
                lastName = null,
                companyName = null,
                roleName = null
            )

            // when
            val result = request.validate()

            // then
            val right = result.shouldBeRight()
            right.firstName shouldBe null
            right.lastName shouldBe null
            right.companyName shouldBe null
            right.roleName shouldBe null
        }
    }

    context("with invalid input") {
        it("should fail with invalid first name") {
            // given
            val request = UserUpdateRequest(
                firstName = "",
                lastName = validLastName,
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
            val request = UserUpdateRequest(
                firstName = validFirstName,
                lastName = "",
                companyName = validCompanyName,
                roleName = validRoleName
            )

            // when
            val result = request.validate()

            // then
            result.shouldBeLeft().shouldBeInstanceOf<NameFailure.Empty>()
        }

        it("should fail with invalid company name") {
            // given
            val request = UserUpdateRequest(
                firstName = validFirstName,
                lastName = validLastName,
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
            val request = UserUpdateRequest(
                firstName = validFirstName,
                lastName = validLastName,
                companyName = validCompanyName,
                roleName = "INVALID_ROLE"
            )

            // when
            val result = request.validate()

            // then
            result.shouldBeLeft().shouldBeInstanceOf<RoleFailure.NotFound>()
        }

        it("should prioritize first name failure when multiple fields are invalid") {
            // given
            val request = UserUpdateRequest(
                firstName = "",
                lastName = "",
                companyName = "",
                roleName = "INVALID_ROLE"
            )

            // when
            val result = request.validate()

            // then
            result.shouldBeLeft().shouldBeInstanceOf<NameFailure.Empty>()
        }

        it("should prioritize last name failure if first name is valid but others are invalid") {
            // given
            val request = UserUpdateRequest(
                firstName = validFirstName,
                lastName = "",
                companyName = "",
                roleName = "INVALID_ROLE"
            )

            // when
            val result = request.validate()

            // then
            result.shouldBeLeft().shouldBeInstanceOf<NameFailure.Empty>()
        }

        it("should prioritize company name failure if names are valid but others are invalid") {
            // given
            val request = UserUpdateRequest(
                firstName = validFirstName,
                lastName = validLastName,
                companyName = "",
                roleName = "INVALID_ROLE"
            )

            // when
            val result = request.validate()

            // then
            result.shouldBeLeft().shouldBeInstanceOf<CompanyNameFailure.Empty>()
        }
    }
})