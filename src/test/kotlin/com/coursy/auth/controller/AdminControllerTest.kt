package com.coursy.auth.controller

import com.coursy.auth.dto.UserResponse
import com.coursy.auth.failure.AuthorizationFailure
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
import org.springframework.test.web.servlet.*
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import kotlin.test.assertNotEquals

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AdminControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val mapper: ObjectMapper,
    private val userRepo: UserRepository,
    private val fixtures: ControllerTestFixtures
) {
    @Nested
    inner class Authorization {
        @Test
        fun `user should not have access to admin endpoint`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_USER)

            // when
            val response = mockMvc.get(fixtures.adminUrl) {
                contentType = MediaType.APPLICATION_JSON
                header("Authorization", "Bearer $jwt")
            }

            // then
            response.andExpect {
                status { isForbidden() }
            }
        }

        @Test
        fun `admin should access admin endpoint`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_ADMIN)

            // when
            val response = mockMvc.get(fixtures.adminUrl) {
                contentType = MediaType.APPLICATION_JSON
                header("Authorization", "Bearer $jwt")
            }

            // then
            response.andExpect {
                status { isOk() }
            }
        }

        @Test
        fun `super-admin should access admin endpoint`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_SUPER_ADMIN)

            // when
            val response = mockMvc.get(fixtures.adminUrl) {
                contentType = MediaType.APPLICATION_JSON
                header("Authorization", "Bearer $jwt")
            }

            // then
            response.andExpect {
                status { isOk() }
            }
        }
    }

    @Nested
    inner class `User registration` {
        @Test
        fun `should save the user`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_ADMIN)
            val registrationRequest = fixtures.regularUserRequest

            // when
            val response = mockMvc.post(fixtures.adminUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(registrationRequest)
                header("Authorization", "Bearer $jwt")
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
            val jwt = fixtures.setupAccount(RoleName.ROLE_ADMIN)
            val registrationRequest = fixtures.regularUserRequest

            // when
            val firstResponse = mockMvc.post(fixtures.adminUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(registrationRequest)
                header("Authorization", "Bearer $jwt")
            }

            val secondResponse = mockMvc.post(fixtures.adminUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(registrationRequest)
                header("Authorization", "Bearer $jwt")
            }

            // then
            firstResponse.andExpect {
                status { isCreated() }
            }
            secondResponse.andExpect {
                status { isConflict() }
            }

            val users = userRepo.findAll()
            assertEquals(2, users.size)
            val user = userRepo.findByEmail(registrationRequest.email)
            assertEquals(registrationRequest.email, user?.email?.value)
        }

        @Test
        fun `should save new admin`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_ADMIN)
            val registrationRequest = fixtures.adminRequest

            // when
            val adminResponse = mockMvc.post(fixtures.adminUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(registrationRequest)
                header("Authorization", "Bearer $jwt")
            }

            // then
            adminResponse.andExpect {
                status { isCreated() }
            }

            val user = userRepo.findByEmail(registrationRequest.email)
            assertNotNull(user)
            assertEquals(registrationRequest.email, user?.email?.value)
            assertEquals(RoleName.ROLE_ADMIN, user?.role?.name)
        }

        @Test
        fun `should return 403 when registering super-admin`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_ADMIN)
            val registrationRequest = fixtures.superAdminRequest

            // when
            val result = mockMvc.post(fixtures.adminUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(registrationRequest)
                header("Authorization", "Bearer $jwt")
            }

            // then
            val response = result.andExpect {
                status { isForbidden() }
            }.andReturn()

            val body = response.response.getContentAsString(StandardCharsets.UTF_8)

            val user = userRepo.findByEmail(registrationRequest.email)
            assertEquals(null, user)
            assertEquals(AuthorizationFailure.InsufficientRole.message(), body)
        }
    }

    @Nested
    inner class `User retrieval` {
        @Test
        fun `should retrieve user`() {
            // given user in DB
            fixtures.setupAccount(RoleName.ROLE_USER)
            val id = userRepo.findByEmail(fixtures.regularUserRequest.email)?.id
            if (id == null) {
                throw IllegalStateException("User not found")
            }
            val jwt = fixtures.setupAccount(RoleName.ROLE_ADMIN)

            // when
            val request = mockMvc.get("${fixtures.adminUrl}/${id}") {
                contentType = MediaType.APPLICATION_JSON
                header("Authorization", "Bearer $jwt")
            }

            // then
            val response = request
                .andExpect {
                    status { isOk() }
                }
                .andReturn()

            val userResponse = mapper.readValue(
                response.response.getContentAsString(StandardCharsets.UTF_8),
                UserResponse::class.java
            )

            assertNotNull(userResponse)
            assertEquals(id, userResponse.id)
        }

        @Test
        fun `should return NOT_FOUND`() {
            // given user not DB
            val id = 99L
            val jwt = fixtures.setupAccount(RoleName.ROLE_ADMIN)

            // when / then
            mockMvc
                .get("${fixtures.adminUrl}/user/$id") {
                    contentType = MediaType.APPLICATION_JSON
                    header("Authorization", "Bearer $jwt")
                }
                .andExpect {
                    status { isNotFound() }
                }
        }
    }

    @Nested
    inner class `User listing` {
        @Test
        fun `should retrieve paginated users`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_ADMIN)
            // Set up multiple users to ensure pagination works
            val userCount = 15
            repeat(userCount) { index ->
                val email = "test.user${index}@example.com"
                val request = fixtures.createRegistrationRequest(
                    firstName = "Test",
                    lastName = "User",
                    email = email,
                )
                mockMvc.post(fixtures.adminUrl) {
                    contentType = MediaType.APPLICATION_JSON
                    content = mapper.writeValueAsString(request)
                    header("Authorization", "Bearer $jwt")
                }
            }

            // when
            val response = mockMvc.get("${fixtures.adminUrl}?page=0&size=5") {
                contentType = MediaType.APPLICATION_JSON
                header("Authorization", "Bearer $jwt")
            }

            // then
            response.andExpect {
                status { isOk() }
                jsonPath("$.page.size") { value(5) }
                jsonPath("$.page.totalElements") { value(16) }
                jsonPath("$.page.number") { value(0) }
            }
        }

        @Test
        fun `should return bad request for invalid page parameters`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_ADMIN)

            // when - test with negative page number
            val response1 = mockMvc.get("${fixtures.adminUrl}?page=-1&size=5") {
                contentType = MediaType.APPLICATION_JSON
                header("Authorization", "Bearer $jwt")
            }

            // then
            response1.andExpect {
                status { isBadRequest() }
            }

            // when - test with negative size
            val response2 = mockMvc.get("${fixtures.adminUrl}?page=0&size=-5") {
                contentType = MediaType.APPLICATION_JSON
                header("Authorization", "Bearer $jwt")
            }

            // then
            response2.andExpect {
                status { isBadRequest() }
            }
        }
    }

    @Nested
    inner class `User update`() {
        @Test
        fun `should update user`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_ADMIN)
            fixtures.setupAccount()
            val email = fixtures.regularEmail
            val userId = userRepo.findByEmail(email)?.id
            val updateRequest = fixtures.userUpdateRequest

            // when
            val response = mockMvc.put("${fixtures.adminUrl}/$userId") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(updateRequest)
                header("Authorization", "Bearer $jwt")
            }

            // then
            response.andExpect {
                status { isOk() }
            }

            val updatedUser = userRepo.findByEmail(email)
            assertNotNull(updatedUser)
            assertEquals(updateRequest.firstName, updatedUser?.firstName?.value)
            assertEquals(updateRequest.lastName, updatedUser?.lastName?.value)
        }

        @Test
        fun `should not update user role`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_ADMIN)
            fixtures.setupAccount()
            val email = fixtures.regularEmail
            val userId = userRepo.findByEmail(email)?.id
            val updateRequest = fixtures.userUpdateRequest.copy(roleName = RoleName.ROLE_ADMIN.toString())

            // when
            val response = mockMvc.put("${fixtures.adminUrl}/$userId") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(updateRequest)
                header("Authorization", "Bearer $jwt")
            }

            // then
            response.andExpect {
                status { isForbidden() }
            }
        }

        @Nested
        inner class `password change` {
            @Test
            fun `should update regular user password`() {
                // given
                val jwt = fixtures.setupAccount(RoleName.ROLE_ADMIN)
                fixtures.setupAccount()
                val email = fixtures.regularEmail
                val userId = userRepo.findByEmail(email)?.id
                val oldPassword = userRepo.findByEmail(email)?.password
                val request = fixtures.changePasswordRequest

                // when
                val response = mockMvc.put(fixtures.adminUrl + "/${userId}/password") {
                    contentType = MediaType.APPLICATION_JSON
                    content = mapper.writeValueAsString(request)
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
            fun `should not update another admin's password`() {
                // given
                val jwt = fixtures.setupAccount(RoleName.ROLE_ADMIN)
                fixtures.setupAccount(RoleName.ROLE_SUPER_ADMIN)
                val email = fixtures.superAdminSetupEmail
                val userId = userRepo.findByEmail(email)?.id
                val oldPassword = userRepo.findByEmail(email)?.password
                val request = fixtures.changePasswordRequest

                // when
                val response = mockMvc.put(fixtures.adminUrl + "/${userId}/password") {
                    contentType = MediaType.APPLICATION_JSON
                    content = mapper.writeValueAsString(request)
                    header("Authorization", "Bearer $jwt")
                }

                // then
                response.andExpect {
                    status { isForbidden() }
                }

                val user = userRepo.findByEmail(email)
                assertEquals(oldPassword, user?.password)
            }
        }
    }

    @Nested
    inner class `User removal` {
        @Test
        fun `should remove user`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_ADMIN)
            fixtures.setupAccount()
            val email = fixtures.regularEmail
            val userId = userRepo.findByEmail(email)?.id

            // when
            val response = mockMvc.delete("${fixtures.adminUrl}/$userId") {
                contentType = MediaType.APPLICATION_JSON
                header("Authorization", "Bearer $jwt")
            }

            // then
            response.andExpect {
                status { isNoContent() }
            }

            val user = userRepo.findById(userId!!)
            assertNotNull(user)
        }

        @Test
        fun `should not remove admin`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_ADMIN)
            fixtures.setupAccount(RoleName.ROLE_SUPER_ADMIN)
            val email = fixtures.superAdminSetupEmail
            val userId = userRepo.findByEmail(email)?.id

            // when
            val response = mockMvc.delete("${fixtures.adminUrl}/$userId") {
                contentType = MediaType.APPLICATION_JSON
                header("Authorization", "Bearer $jwt")
            }

            // then
            response.andExpect {
                status { isForbidden() }
            }

            val user = userRepo.findByEmail(email)
            assertNotNull(user)
        }
    }
}