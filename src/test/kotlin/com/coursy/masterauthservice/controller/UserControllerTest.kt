package com.coursy.masterauthservice.controller

import com.coursy.masterauthservice.dto.JwtResponse
import com.coursy.masterauthservice.dto.UserResponse
import com.coursy.masterauthservice.failure.AuthorizationFailure
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
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UserControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val mapper: ObjectMapper,
    private val userRepo: UserRepository,
    private val userController: UserController
) {
    private val fixtures = ControllerTestFixtures()

    @Nested
    inner class `Authorization` {
        @Nested
        inner class `An user` {
            @Test
            fun `should access user endpoint`() {
                // given
                val jwt = setupAccount(RoleName.ROLE_USER)

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
            fun `should not have access to admin endpoint`() {
                // given
                val jwt = setupAccount(RoleName.ROLE_USER)

                // when
                val response = mockMvc.post(fixtures.adminUrl) {
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
            fun `should not have access to super-admin endpoint`() {
                // given
                val jwt = setupAccount(RoleName.ROLE_USER)

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
        }

        @Nested
        inner class `An admin` {
            @Test
            fun `should access user endpoint`() {
                // given
                val jwt = setupAccount(RoleName.ROLE_ADMIN)

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
            fun `should access admin endpoint`() {
                // given
                val jwt = setupAccount(RoleName.ROLE_ADMIN)

                // when
                val response = mockMvc.post(fixtures.adminUrl) {
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
            fun `should not have access to super-admin endpoint`() {
                // given
                val jwt = setupAccount(RoleName.ROLE_ADMIN)

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
        }

        @Nested
        inner class `A super-admin` {
            @Test
            fun `should access user endpoint`() {
                // given
                val jwt = setupAccount(RoleName.ROLE_SUPER_ADMIN)

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
            fun `should access admin endpoint`() {
                // given
                val jwt = setupAccount(RoleName.ROLE_SUPER_ADMIN)

                // when
                val response = mockMvc.post(fixtures.adminUrl) {
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
            fun `should access super-admin endpoint`() {
                // given
                val jwt = setupAccount(RoleName.ROLE_SUPER_ADMIN)

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
    }

    @Nested
    inner class `User registration` {
        @Nested
        inner class `When registering regular user` {
            @Nested
            inner class `from user's point of view` {
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
            }

            @Nested
            inner class `from admin's point of view` {
                @Test
                fun `should save the user`() {
                    // given
                    val jwt = setupAccount(RoleName.ROLE_ADMIN)
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
                    val jwt = setupAccount(RoleName.ROLE_ADMIN)
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
            }

            @Nested
            inner class `from super-admin's point of view` {
                @Test
                fun `should save the user`() {
                    // given
                    val jwt = setupAccount(RoleName.ROLE_SUPER_ADMIN)
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
                    val jwt = setupAccount(RoleName.ROLE_SUPER_ADMIN)
                    val registrationRequest = fixtures.regularUserRequest

                    // when
                    val firstResponse = mockMvc.post(fixtures.superAdminUrl) {
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
            }
        }

        @Nested
        inner class `When registering admin` {
            @Nested
            inner class `from user's point of view` {
                @Test
                fun `should create regular user anyway`() {
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
            }

            @Nested
            inner class `from admin's point of view` {
                @Test
                fun `should save new admin`() {
                    // given
                    val jwt = setupAccount(RoleName.ROLE_ADMIN)
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
            }

            @Nested
            inner class `from super-admin's point of view` {
                @Test
                fun `should save new admin`() {
                    // given
                    val jwt = setupAccount(RoleName.ROLE_SUPER_ADMIN)
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
            }
        }

        @Nested
        inner class `When registering super-admin` {
            @Nested
            inner class `from admin's point of view` {
                @Test
                fun `should return 403`() {
                    // given
                    val jwt = setupAccount(RoleName.ROLE_SUPER_ADMIN)
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
        }

        @Nested
        inner class `from super-admin's point of view` {
            @Test
            fun `should save new super-admin`() {
                // given
                val jwt = setupAccount(RoleName.ROLE_SUPER_ADMIN)

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

            @Test
            fun `should not save 2 super_admins with the same email`() {
                val jwt = setupAccount(RoleName.ROLE_SUPER_ADMIN)
                val registrationRequest = fixtures.superAdminSetupRequest

                // when
                val adminResponse = mockMvc.post(fixtures.userUrl) {
                    contentType = MediaType.APPLICATION_JSON
                    content = mapper.writeValueAsString(registrationRequest)
                    header("Authorization", "Bearer $jwt")
                }
                val users = userRepo.findAll()

                // then
                adminResponse.andExpect {
                    status { isConflict() }
                }
                assertEquals(1, users.size)
            }
        }
    }

    @Nested
    inner class `User retrieval` {
        @Nested
        inner class `single user`() {
            @Test
            fun `should retrieve user`() {
                // given user in DB
                setupAccount(RoleName.ROLE_USER)
                val id = userRepo.findByEmail(fixtures.regularUserRequest.email)?.id
                if (id == null) {
                    throw IllegalStateException("User not found")
                }
                val jwt = setupAccount(RoleName.ROLE_ADMIN)

                // when
                val request = mockMvc.get("${fixtures.adminUrl}/$id") {
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
            fun `should return NOT_FONUD`() {
                // given user not DB
                val id = 99L
                val jwt = setupAccount(RoleName.ROLE_ADMIN)

                // when / then
                mockMvc
                    .get("${fixtures.adminUrl}/$id") {
                        contentType = MediaType.APPLICATION_JSON
                        header("Authorization", "Bearer $jwt")
                    }
                    .andExpect {
                        status { isNotFound() }
                    }
            }
        }
    }

    private fun setupAccount(role: RoleName = RoleName.ROLE_USER): String {
        val registerRequest = when (role) {
            RoleName.ROLE_SUPER_ADMIN -> fixtures.superAdminSetupRequest
            RoleName.ROLE_ADMIN -> fixtures.adminSetupRequest
            else -> fixtures.regularUserRequest
        }
        userController.createSuperUser(registerRequest)

        val loginRequest = registerRequest.toLoginRequest()

        val authResponse = mockMvc.post("${fixtures.authUrl}/login") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(loginRequest)
        }.andExpect {
            status { isOk() }
        }
            .andReturn()

        val jwtResponse = mapper.readValue(
            authResponse.response.getContentAsString(StandardCharsets.UTF_8),
            JwtResponse::class.java
        )

        return jwtResponse.token
    }
}