package com.coursy.masterauthservice.controller

import com.coursy.masterauthservice.model.RoleName
import com.coursy.masterauthservice.repository.UserRepository
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
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse
import kotlin.test.assertNotEquals

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class SuperAdminControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val mapper: ObjectMapper,
    private val userRepo: UserRepository,
    private val fixtures: ControllerTestFixtures
) {
    @Nested
    inner class Authorization {
        @Test
        fun `user should not have access to super-admin endpoint`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_USER)

            // when
            val response = mockMvc.post(fixtures.superAdminUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = null
                header("Authorization", "Bearer $jwt")
            }

            // then
            response.andExpect {
                status { isForbidden() }
            }
        }

        @Test
        fun `admin should not have access to super-admin endpoint`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_ADMIN)

            // when
            val response = mockMvc.post(fixtures.superAdminUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = null
                header("Authorization", "Bearer $jwt")
            }

            // then
            response.andExpect {
                status { isForbidden() }
            }
        }

        @Test
        fun `super-admin should access super-admin endpoint`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_SUPER_ADMIN)

            // when
            val response = mockMvc.post(fixtures.superAdminUrl) {
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
            val jwt = fixtures.setupAccount(RoleName.ROLE_SUPER_ADMIN)
            val registrationRequest = fixtures.regularUserRequest

            // when
            val response = mockMvc.post(fixtures.superAdminUrl) {
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
            val jwt = fixtures.setupAccount(RoleName.ROLE_SUPER_ADMIN)
            val registrationRequest = fixtures.regularUserRequest

            // when
            val firstResponse = mockMvc.post(fixtures.superAdminUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(registrationRequest)
                header("Authorization", "Bearer $jwt")
            }

            val secondResponse = mockMvc.post(fixtures.superAdminUrl) {
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
            val jwt = fixtures.setupAccount(RoleName.ROLE_SUPER_ADMIN)
            val registrationRequest = fixtures.adminRequest

            // when
            val adminResponse = mockMvc.post(fixtures.superAdminUrl) {
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
        fun `should save new super-admin`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_SUPER_ADMIN)
            val registrationRequest = fixtures.superAdminRequest

            // when
            val response = mockMvc.post(fixtures.superAdminUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(registrationRequest)
                header("Authorization", "Bearer $jwt")
            }

            // then
            response.andExpect {
                status { isCreated() }
            }

            val user = userRepo.findByEmail(registrationRequest.email)
            assertNotNull(user)
            assertEquals(registrationRequest.email, user?.email?.value)
            assertEquals(RoleName.ROLE_SUPER_ADMIN, user?.role?.name)
        }
    }

    @Nested
    inner class `User update` {
        @Test
        fun `should update user role`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_SUPER_ADMIN)
            fixtures.setupAccount()
            val email = fixtures.regularEmail
            val userId = userRepo.findByEmail(email)?.id

            val updateRequest = fixtures.userUpdateRequest.copy(roleName = RoleName.ROLE_ADMIN.toString())

            // when
            val response = mockMvc.put("${fixtures.superAdminUrl}/${userId}") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(updateRequest)
                header("Authorization", "Bearer $jwt")
            }

            // then
            response.andExpect {
                status { isOk() }
            }

            val updatedUser = userRepo.findById(userId!!)
                .getOrElse { throw IllegalStateException("User not found") }
            assertEquals(RoleName.ROLE_ADMIN, updatedUser.role.name)
        }

        @Test
        fun `should update another admin's password`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_SUPER_ADMIN)
            fixtures.setupAccount(RoleName.ROLE_ADMIN)
            val email = fixtures.adminSetupEmail
            val userId = userRepo.findByEmail(email)?.id
            val oldPassword = userRepo.findByEmail(email)?.password

            val updateRequest = fixtures.changePasswordRequest

            // when
            val response = mockMvc.put("${fixtures.superAdminUrl}/${userId}/password") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(updateRequest)
                header("Authorization", "Bearer $jwt")
            }

            // then
            response.andExpect {
                status { isOk() }
            }

            val updatedUser = userRepo.findById(userId!!)
                .getOrElse { throw IllegalStateException("User not found") }
            assertNotEquals(oldPassword, updatedUser.password)
        }
    }

    @Nested
    inner class `User removal` {
        @Test
        fun `should remove admin`() {
            // given
            val jwt = fixtures.setupAccount(RoleName.ROLE_SUPER_ADMIN)
            fixtures.setupAccount(RoleName.ROLE_ADMIN)
            val email = fixtures.adminSetupEmail
            val userId = userRepo.findByEmail(email)?.id

            // when
            val response = mockMvc.delete("${fixtures.superAdminUrl}/$userId") {
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
    }
}