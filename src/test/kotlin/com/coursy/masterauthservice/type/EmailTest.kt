package com.coursy.masterauthservice.type

import com.coursy.masterauthservice.failure.EmailFailure
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class EmailTest : DescribeSpec({
    describe("Validation") {
        it("should create valid email") {
            // given
            val value = "valid.email@gmail.com"

            // when
            val emailResult = Email.create(value)

            // then
            val right = emailResult.shouldBeRight()
            right.toString() shouldBe value
        }

        describe("Length") {
            it("should not create email with length = maxLength + 1") {
                // given
                val value = getTooLongValue()

                // when
                val emailResult = Email.create(value)

                // then
                emailResult.shouldBeLeft()
                    .shouldBeInstanceOf<EmailFailure.TooLong>()
            }

            it("should create email with length = maxLength") {
                // given
                val value = getMaxLengthValue()

                // when
                val emailResult = Email.create(value)

                // then
                val right = emailResult.shouldBeRight()
                right.toString() shouldBe value
            }

            it("should not create email with length = minlength - 1") {
                // given
                val value = getTooShortValue()

                // when
                val emailResult = Email.create(value)

                // then
                emailResult.shouldBeLeft()
                    .shouldBeInstanceOf<EmailFailure.TooShort>()
            }

            it("should create email with length = minlength") {
                // given
                val value = getMinLengthValue()

                // when
                val emailResult = Email.create(value)

                // then
                val right = emailResult.shouldBeRight()
                right.toString() shouldBe value
            }
        }

        describe("Format") {
            it("should not create email without @") {
                // given
                val value = "testmailatgmail.com"

                // when
                val emailResult = Email.create(value)

                // then
                emailResult.shouldBeLeft()
                    .shouldBeInstanceOf<EmailFailure.MissingAtSymbol>()
            }

            it("should not create email with too short domain name") {
                // given
                val value = "test@gmail.c"

                // when
                val emailResult = Email.create(value)

                // then
                emailResult.shouldBeLeft()
                    .shouldBeInstanceOf<EmailFailure.InvalidFormat>()
            }

            it("should not create email with too long domain name") {
                // given
                val value = "test@gmail.cooom"

                // when
                val emailResult = Email.create(value)

                // then
                emailResult.shouldBeLeft()
                    .shouldBeInstanceOf<EmailFailure.InvalidFormat>()
            }

            it("should  create email with dots") {
                // given
                val value = "test.email@gmail.com"

                // when
                val emailResult = Email.create(value)

                // then
                val right = emailResult.shouldBeRight()
                right.toString() shouldBe value
            }
        }
    }
}) {
    companion object {
        // len = 61
        private fun getTooLongValue() = "aaaaaaaaaabbbbbbbbbbccccccccccddddddddddeeeeeeeeeegggg@em.com"

        // len = 60
        private fun getMaxLengthValue() = "aaaaaaaaaabbbbbbbbbbccccccccccddddddddddeeeeeeeeeegg@em.com"

        // len = 5
        private fun getTooShortValue() = "a@a.a"

        // len = 6
        private fun getMinLengthValue() = "a@a.aa"
    }
}