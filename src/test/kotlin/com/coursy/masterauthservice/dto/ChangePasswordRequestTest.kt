package com.coursy.masterauthservice.dto

import com.coursy.masterauthservice.failure.PasswordFailure
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class ChangePasswordRequestTest : DescribeSpec({

    describe("ChangePasswordRequest") {
        context("when validating the request") {
            it("should pass the validation") {
                // given
                val validPassword = "pa\$\$w0RD"
                val request = ChangePasswordRequest(password = validPassword)

                // when
                val result = request.validate()

                // then
                val right = result.shouldBeRight()
                right.password.value shouldBe validPassword
            }

            it("should not pass the validation for invalid password") {
                // given
                val invalidPassword = "password"
                val request = ChangePasswordRequest(password = invalidPassword)

                // when
                val result = request.validate()

                // then
                val right = result.shouldBeLeft()
                    .shouldBeInstanceOf<PasswordFailure>()
            }
        }
    }
})