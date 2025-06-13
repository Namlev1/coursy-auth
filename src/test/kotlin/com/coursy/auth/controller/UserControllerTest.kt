package com.coursy.auth.controller

import com.coursy.auth.dto.ChangePasswordRequest
import com.coursy.auth.model.RoleName
import com.coursy.auth.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertNotEquals

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UserControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val mapper: ObjectMapper,
    private val userRepo: UserRepository,
    private val fixtures: ControllerTestFixtures
) {

    @Nested
    inner class Authorization {
        @Test
        fun `user should access user endpoint`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_USER)

            // when
            val response = mockMvc.post(fixtures.userUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = null
                header("Authorization", "Bearer $jwt")
            }

            // then
            response.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        fun `admin should access user endpoint`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_ADMIN)

            // when
            val response = mockMvc.post(fixtures.userUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = null
                header("Authorization", "Bearer $jwt")
            }

            // then
            response.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        fun `super-admin should access user endpoint`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_SUPER_ADMIN)

            // when
            val response = mockMvc.post(fixtures.userUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = null
                header("Authorization", "Bearer $jwt")
            }

            // then
            response.andExpect {
                status { isBadRequest() }
            }
        }
    }

    @Nested
    inner class `User registration` {
        @Test
        fun `should save the user`() {
            // given
            val registrationRequest = fixtures.regularUserRequest

            // when
            val response = mockMvc.post(fixtures.userUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(registrationRequest)
            }

            // then
            response.andExpect {
                status { isCreated() }
            }

            val savedUser = userRepo.findByEmail(registrationRequest.email)
            assertNotNull(savedUser)
            assertEquals(registrationRequest.firstName, savedUser?.firstName?.value)
            assertEquals(registrationRequest.lastName, savedUser?.lastName?.value)
        }

        @Test
        fun `should not save 2 users with the same email`() {
            // given
            val registrationRequest = fixtures.regularUserRequest

            // when
            val firstResponse = mockMvc.post(fixtures.userUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(registrationRequest)
            }

            val secondResponse = mockMvc.post(fixtures.userUrl) {
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
            assertEquals(1, users.size)
            assertEquals(registrationRequest.email, users[0].email.value)
        }

        @Test
        fun `should create regular user when admin role requested`() {
            val registrationRequest = fixtures.adminRequest

            // when
            val adminResponse = mockMvc.post(fixtures.userUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(registrationRequest)
            }

            // then
            adminResponse.andExpect {
                status { isCreated() }
            }
            val user = userRepo.findByEmail(registrationRequest.email)
            assertEquals(RoleName.ROLE_USER, user?.role?.name)
        }

        @Test
        fun `should not create user if password is too weak`() {
            // given
            val registrationRequest = fixtures.createRegistrationRequest(password = "weak password")

            // when
            val response = mockMvc.post(fixtures.userUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(registrationRequest)
            }

            // then
            response.andExpect {
                status { isBadRequest() }
            }
        }
    }

    @Nested
    inner class `Current user data manipulation` {
        @Test
        fun `should retrieve current user data`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_USER)

            // when
            val response = mockMvc.get(fixtures.userUrl + "/me") {
                contentType = MediaType.APPLICATION_JSON
                content = null
                header("Authorization", "Bearer $jwt")
            }

            // then
            response.andExpect {
                status { isOk() }
                jsonPath("$.email") { value(fixtures.regularUserRequest.email) }
                jsonPath("$.firstName") { value(fixtures.regularUserRequest.firstName) }
                jsonPath("$.lastName") { value(fixtures.regularUserRequest.lastName) }
            }

        }

        @Test
        fun `should not retrieve current user data when not authenticated`() {
            // when
            val response = mockMvc.get(fixtures.userUrl + "/me") {
                contentType = MediaType.APPLICATION_JSON
            }

            // then
            response.andExpect {
                status { isForbidden() }
            }
        }

        @Test
        fun `should change password`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_USER)
            val oldPassword = userRepo.findByEmail(fixtures.regularUserRequest.email)?.password
            val changePasswordRequest = fixtures.changePasswordRequest

            // when
            val response = mockMvc.put(fixtures.userUrl + "/me/password") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(changePasswordRequest)
                header("Authorization", "Bearer $jwt")
            }

            // then
            response.andExpect {
                status { isOk() }
            }

            val user = userRepo.findByEmail(fixtures.regularUserRequest.email)
            assertNotEquals(oldPassword, user?.password)
        }

        @Test
        fun `should not change password if too weak`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_USER)
            val oldPassword = userRepo.findByEmail(fixtures.regularUserRequest.email)?.password
            val changePasswordRequest = ChangePasswordRequest(password = "too weak password")

            // when
            val response = mockMvc.put(fixtures.userUrl + "/me/password") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(changePasswordRequest)
                header("Authorization", "Bearer $jwt")
            }

            // then
            response.andExpect {
                status { isBadRequest() }
            }

            val user = userRepo.findByEmail(fixtures.regularUserRequest.email)
            assertEquals(oldPassword, user?.password)
        }
    }
}