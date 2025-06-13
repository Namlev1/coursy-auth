package com.coursy.auth.type

import arrow.core.Either
import com.coursy.auth.failure.PasswordFailure
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class PasswordTest : DescribeSpec({
    describe("Validation") {
        it("should create valid password") {
            // given
            val value = "Valid1Password!"

            // when
            val passwordResult: Either<PasswordFailure, Password> = Password.create(value)

            // then
            val right = passwordResult.shouldBeRight()
            right.value shouldBe value
        }

        describe("Length") {
            it("should not create password with length = maxLength + 1") {
                // given
                val value = getTooLongValue()

                // when
                val passwordResult = Password.create(value)

                // then
                passwordResult.shouldBeLeft()
                    .shouldBeInstanceOf<PasswordFailure.TooLong>()
            }

            it("should create password with length = maxLength") {
                // given
                val value = getMaxLengthValue()

                // when
                val passwordResult = Password.create(value)

                // then
                val right = passwordResult.shouldBeRight()
                right.value shouldBe value
            }

            it("should not create password with length = minLength - 1") {
                // given
                val value = getTooShortValue()

                // when
                val passwordResult = Password.create(value)

                // then
                passwordResult.shouldBeLeft()
                    .shouldBeInstanceOf<PasswordFailure.TooShort>()
            }

            it("should create password with length = minLength") {
                // given
                val value = getMinLengthValue()

                // when
                val passwordResult = Password.create(value)

                // then
                val right = passwordResult.shouldBeRight()
                right.value shouldBe value
            }
        }

        describe("Complexity") {
            it("should not create password without uppercase letter") {
                // given
                val value = "password1!@#"

                // when
                val passwordResult = Password.create(value)

                // then
                val left = passwordResult.shouldBeLeft()
                    .shouldBeInstanceOf<PasswordFailure.InsufficientComplexity>()
                left.errors.shouldContainExactly(PasswordFailure.ComplexityFailure.MissingUppercase)
            }

            it("should not create password without lowercase letter") {
                // given
                val value = "PASSWORD1!@#"

                // when
                val passwordResult = Password.create(value)

                // then
                val left = passwordResult.shouldBeLeft()
                    .shouldBeInstanceOf<PasswordFailure.InsufficientComplexity>()
                left.errors.shouldContainExactly(PasswordFailure.ComplexityFailure.MissingLowercase)
            }

            it("should not create password without digit") {
                // given
                val value = "Password!@#"

                // when
                val passwordResult = Password.create(value)

                // then
                val left = passwordResult.shouldBeLeft()
                    .shouldBeInstanceOf<PasswordFailure.InsufficientComplexity>()
                left.errors.shouldContainExactly(PasswordFailure.ComplexityFailure.MissingDigit)
            }

            it("should not create password without special character") {
                // given
                val value = "Password123"

                // when
                val passwordResult = Password.create(value)

                // then
                val left = passwordResult.shouldBeLeft()
                    .shouldBeInstanceOf<PasswordFailure.InsufficientComplexity>()
                left.errors.shouldContainExactly(PasswordFailure.ComplexityFailure.MissingSpecialChar)
            }

            it("should not create password with multiple complexity failures") {
                // given
                val value = "password"  // missing uppercase, digit, and special character

                // when
                val passwordResult = Password.create(value)

                // then
                val left = passwordResult.shouldBeLeft()
                    .shouldBeInstanceOf<PasswordFailure.InsufficientComplexity>()
                left.errors.size shouldBe 3
                left.errors.shouldContain(PasswordFailure.ComplexityFailure.MissingUppercase)
                left.errors.shouldContain(PasswordFailure.ComplexityFailure.MissingDigit)
                left.errors.shouldContain(PasswordFailure.ComplexityFailure.MissingSpecialChar)
            }
        }

        describe("Pattern") {
            it("should not create password with repeating characters") {
                // given
                val value = "Passsword1!"  // three consecutive 's'

                // when
                val passwordResult = Password.create(value)

                // then
                passwordResult.shouldBeLeft()
                    .shouldBeInstanceOf<PasswordFailure.RepeatingCharacters>()
            }
        }

        describe("Empty") {
            it("should not create empty password") {
                // given
                val value = ""

                // when
                val passwordResult = Password.create(value)

                // then
                passwordResult.shouldBeLeft()
                    .shouldBeInstanceOf<PasswordFailure.Empty>()
            }
        }
    }
}) {
    companion object {
        private fun getTooLongValue(): String {
            // 73 characters, with all required complexity elements
            return "cEbZ8qFhFDVkGTLfVC7YUaHZ0c10uIOjcEbZ8qFhFDVkGTLfVC7YUaHZ0c10uIOjaoc!tlmny"
        }

        private fun getMaxLengthValue(): String {
            // 72 characters, with all required complexity elements
            return "cEbZ8qFhFDVkGTLfVC7YUaHZ0c10uIOjcEbZ8qFhFDVkGTLfVC7YUaHZ0c10uIOjaoc!tlmn"
        }

        private fun getTooShortValue(): String {
            // 7 characters, with all required complexity elements
            return "Aab1!@"
        }

        private fun getMinLengthValue(): String {
            // 8 characters, with all required complexity elements
            return "Aab1!@#w"
        }

        private fun List<PasswordFailure.ComplexityFailure>.shouldContainExactly(
            expected: PasswordFailure.ComplexityFailure
        ) {
            this.size shouldBe 1
            this.first() shouldBe expected
        }

        private fun List<PasswordFailure.ComplexityFailure>.shouldContain(
            expected: PasswordFailure.ComplexityFailure
        ) {
            this.any { it == expected } shouldBe true
        }
    }
}