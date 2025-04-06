package com.coursy.masterauthservice.type

import com.coursy.masterauthservice.failure.CompanyNameFailure
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class CompanyNameTest : DescribeSpec({
    describe("Validation") {
        it("should create valid company name") {
            // given
            val value = "Acme Corp"

            // when
            val companyNameResult = CompanyName.create(value)

            // then
            val right = companyNameResult.shouldBeRight()
            right.value shouldBe value
        }

        describe("Length") {
            it("should not create company name with length = maxLength + 1") {
                // given
                val value = getTooLongValue()

                // when
                val companyNameResult = CompanyName.create(value)

                // then
                companyNameResult.shouldBeLeft()
                    .shouldBeInstanceOf<CompanyNameFailure.TooLong>()
            }

            it("should create company name with length = maxLength") {
                // given
                val value = getMaxLengthValue()

                // when
                val companyNameResult = CompanyName.create(value)

                // then
                val right = companyNameResult.shouldBeRight()
                right.value shouldBe value
            }

            it("should not create company name with length = minlength - 1") {
                // given
                val value = getTooShortValue()

                // when
                val companyNameResult = CompanyName.create(value)

                // then
                companyNameResult.shouldBeLeft()
                    .shouldBeInstanceOf<CompanyNameFailure.TooShort>()
            }

            it("should create company name with length = minlength") {
                // given
                val value = getMinLengthValue()

                // when
                val companyNameResult = CompanyName.create(value)

                // then
                val right = companyNameResult.shouldBeRight()
                right.value shouldBe value
            }
        }

        describe("Format") {
            it("should create company name with numbers") {
                // given
                val value = "Company123"

                // when
                val companyNameResult = CompanyName.create(value)

                // then
                val right = companyNameResult.shouldBeRight()
                right.value shouldBe value
            }

            it("should not create company name with invalid special characters") {
                // given
                val value = "Company*Corp"

                // when
                val companyNameResult = CompanyName.create(value)

                // then
                companyNameResult.shouldBeLeft()
                    .shouldBeInstanceOf<CompanyNameFailure.InvalidFormat>()
            }

            it("should create company name with spaces") {
                // given
                val value = "Acme Corporation"

                // when
                val companyNameResult = CompanyName.create(value)

                // then
                val right = companyNameResult.shouldBeRight()
                right.value shouldBe value
            }

            it("should create company name with hyphens") {
                // given
                val value = "Coca-Cola"

                // when
                val companyNameResult = CompanyName.create(value)

                // then
                val right = companyNameResult.shouldBeRight()
                right.value shouldBe value
            }

            it("should create company name with apostrophes") {
                // given
                val value = "McDonald's"

                // when
                val companyNameResult = CompanyName.create(value)

                // then
                val right = companyNameResult.shouldBeRight()
                right.value shouldBe value
            }

            it("should create company name with ampersands") {
                // given
                val value = "Johnson & Johnson"

                // when
                val companyNameResult = CompanyName.create(value)

                // then
                val right = companyNameResult.shouldBeRight()
                right.value shouldBe value
            }

            it("should create company name with dots") {
                // given
                val value = "Amazon.com"

                // when
                val companyNameResult = CompanyName.create(value)

                // then
                val right = companyNameResult.shouldBeRight()
                right.value shouldBe value
            }

            it("should create company name with commas") {
                // given
                val value = "Smith, Jones, and Associates"

                // when
                val companyNameResult = CompanyName.create(value)

                // then
                val right = companyNameResult.shouldBeRight()
                right.value shouldBe value
            }

            it("should create company name with at sign") {
                // given
                val value = "Solutions@Work"

                // when
                val companyNameResult = CompanyName.create(value)

                // then
                val right = companyNameResult.shouldBeRight()
                right.value shouldBe value
            }

            it("should create company name with accented characters") {
                // given
                val value = "Café Noir"

                // when
                val companyNameResult = CompanyName.create(value)

                // then
                val right = companyNameResult.shouldBeRight()
                right.value shouldBe value
            }
        }

        describe("Empty") {
            it("should not create empty company name") {
                // given
                val value = ""

                // when
                val companyNameResult = CompanyName.create(value)

                // then
                companyNameResult.shouldBeLeft()
                    .shouldBeInstanceOf<CompanyNameFailure.Empty>()
            }

            it("should not create blank company name") {
                // given
                val value = "   "

                // when
                val companyNameResult = CompanyName.create(value)

                // then
                companyNameResult.shouldBeLeft()
                    .shouldBeInstanceOf<CompanyNameFailure.Empty>()
            }
        }
    }
}) {
    companion object {
        // len = 101
        private fun getTooLongValue() = "A".repeat(101)

        // len = 100
        private fun getMaxLengthValue() = "A".repeat(100)

        // len = 1
        private fun getTooShortValue() = "A"

        // len = 2
        private fun getMinLengthValue() = "Ab"
    }
}