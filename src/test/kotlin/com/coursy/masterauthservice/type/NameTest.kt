package com.coursy.masterauthservice.type

import arrow.core.Either
import com.coursy.masterauthservice.failure.NameFailure
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class NameTest : DescribeSpec({
    describe("Validation") {
        it("should create valid name") {
            // given
            val value = "John"

            // when
            val nameResult: Either<NameFailure, Name> = Name.create(value)

            // then
            val right = nameResult.shouldBeRight()
            right.value shouldBe value
        }

        describe("Length") {
            it("should not create name with length = maxLength + 1") {
                // given
                val value = getTooLongValue()

                // when
                val nameResult = Name.create(value)

                // then
                nameResult.shouldBeLeft()
                    .shouldBeInstanceOf<NameFailure.TooLong>()
            }

            it("should create name with length = maxLength") {
                // given
                val value = getMaxLengthValue()

                // when
                val nameResult = Name.create(value)

                // then
                val right = nameResult.shouldBeRight()
                right.value shouldBe value
            }

            it("should not create name with length = minlength - 1") {
                // given
                val value = getTooShortValue()

                // when
                val nameResult = Name.create(value)

                // then
                nameResult.shouldBeLeft()
                    .shouldBeInstanceOf<NameFailure.TooShort>()
            }

            it("should create name with length = minlength") {
                // given
                val value = getMinLengthValue()

                // when
                val nameResult = Name.create(value)

                // then
                val right = nameResult.shouldBeRight()
                right.value shouldBe value
            }
        }

        describe("Format") {
            it("should not create name with numbers") {
                // given
                val value = "John123"

                // when
                val nameResult = Name.create(value)

                // then
                nameResult.shouldBeLeft()
                    .shouldBeInstanceOf<NameFailure.InvalidFormat>()
            }

            it("should not create name with special characters") {
                // given
                val value = "John@Doe"

                // when
                val nameResult = Name.create(value)

                // then
                nameResult.shouldBeLeft()
                    .shouldBeInstanceOf<NameFailure.InvalidFormat>()
            }

            it("should create name with spaces") {
                // given
                val value = "Jean Claude"

                // when
                val nameResult = Name.create(value)

                // then
                val right = nameResult.shouldBeRight()
                right.value shouldBe value
            }

            it("should create name with hyphens") {
                // given
                val value = "Anne-Marie"

                // when
                val nameResult = Name.create(value)

                // then
                val right = nameResult.shouldBeRight()
                right.value shouldBe value
            }

            it("should create name with apostrophes") {
                // given
                val value = "O'Connor"

                // when
                val nameResult = Name.create(value)

                // then
                val right = nameResult.shouldBeRight()
                right.value shouldBe value
            }

            it("should create name with accented characters") {
                // given
                val value = "José"

                // when
                val nameResult = Name.create(value)

                // then
                val right = nameResult.shouldBeRight()
                right.value shouldBe value
            }

            it("should create name starting with small character") {
                // given
                val value = "john"

                // when
                val nameResult: Either<NameFailure, Name> = Name.create(value)

                // then
                val right = nameResult.shouldBeRight()
                right.value shouldBe value
            }
        }

        describe("Empty") {
            it("should not create empty name") {
                // given
                val value = ""

                // when
                val nameResult = Name.create(value)

                // then
                nameResult.shouldBeLeft()
                    .shouldBeInstanceOf<NameFailure.Empty>()
            }
        }
    }
}) {
    companion object {
        // len = 51
        private fun getTooLongValue() = "Abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxy"

        // len = 50
        private fun getMaxLengthValue() = "Abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwx"

        // len = 1
        private fun getTooShortValue() = "A"

        // len = 2
        private fun getMinLengthValue() = "Ab"
    }
}