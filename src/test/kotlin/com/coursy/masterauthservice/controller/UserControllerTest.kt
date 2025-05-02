package com.coursy.masterauthservice.controller

import com.coursy.masterauthservice.dto.RegistrationRequest
import com.coursy.masterauthservice.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UserControllerTest(
    private val mockMvc: MockMvc,
    private val mapper: ObjectMapper,
    private val userRepo: UserRepository,
    private val userController: UserController
) : DescribeSpec({
    val url = "/v1/user"

    describe("User registration") {
        context("When registering regular user") {
            context("with correct data") {
                it("should save the user") {
                    // given
                    val registrationRequest = RegistrationRequest(
                        firstName = "Jan",
                        lastName = "Kowalski",
                        email = "jan.kowalski@example.com",
                        password = "SecurePassword123!",
                        companyName = null,
                        roleName = null
                    )

                    // when
                    val response = mockMvc.post(url) {
                        contentType = MediaType.APPLICATION_JSON
                        content = mapper.writeValueAsString(registrationRequest)
                    }


                    // then
                    response.andExpect {
                        status { isCreated() }
                    }
                    val savedUser = userRepo.findByEmail(registrationRequest.email)
                    savedUser shouldNotBe null
                    savedUser?.firstName?.value shouldBe registrationRequest.firstName
                    savedUser?.lastName?.value shouldBe registrationRequest.lastName
                }

                it("should not save 2 users with the same email") {
                    // given
                    val registrationRequest = RegistrationRequest(
                        firstName = "Jan",
                        lastName = "Kowalski",
                        email = "jan.kowalski@example.com",
                        password = "SecurePassword123!",
                        companyName = null,
                        roleName = null
                    )

                    // when
                    val firstResponse = mockMvc.post(url) {
                        contentType = MediaType.APPLICATION_JSON
                        content = mapper.writeValueAsString(registrationRequest)
                    }
                    val secondResponse = mockMvc.post(url) {
                        contentType = MediaType.APPLICATION_JSON
                        content = mapper.writeValueAsString(registrationRequest)
                    }


                    // then
                    firstResponse.andExpect {
                        status { isCreated() }
                    }
                    secondResponse.andExpect {
                        status { isConflict() }
                    }
                    val users = userRepo.findAll()
                    users.size shouldBe 1
                    users[0].email.value shouldBe registrationRequest.email
                }
            }
        }
        context("When registering admin") {
        }

        context("When registering user with incorrect data") {

        }
    }
})
