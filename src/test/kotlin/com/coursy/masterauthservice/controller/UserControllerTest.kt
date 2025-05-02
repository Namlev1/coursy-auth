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
    private val userRepo: UserRepository
) : DescribeSpec({
    val url = "/v1/user"

    describe("User registration") {
        context("When registering regular user with correct data") {
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
                mockMvc.post("/v1/user") {
                    contentType = MediaType.APPLICATION_JSON
                    content = mapper.writeValueAsString(registrationRequest)
                }.andExpect {
                    status { isCreated() }
                }

                // then
                val savedUser = userRepo.findByEmail(registrationRequest.email)
                savedUser shouldNotBe null
                savedUser?.firstName?.value shouldBe registrationRequest.firstName
                savedUser?.lastName?.value shouldBe registrationRequest.lastName
            }

        }
        context("When registering non-regular user with correct data") {

        }

        context("When registering user with incorrect data") {

        }
    }
})
