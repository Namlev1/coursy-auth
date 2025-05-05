package com.coursy.masterauthservice.controller

import com.coursy.masterauthservice.dto.JwtResponse
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
    inner class `User registration` {

        @Nested
        inner class `When registering regular user` {

            @Nested
            inner class `with correct data` {

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
                @Nested
                inner class `with correct data` {
                    @Test
                    fun `should save the user`() {
                        // given
                        val jwt = setupAdmin()
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
                        val jwt = setupAdmin()
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
                        assertEquals(1, users.size)
                        assertEquals(registrationRequest.email, users[0].email.value)
                    }
                }
            }
        }

        @Nested
        inner class `When registering admin` {

            @Nested
            inner class `with correct data` {
                @Test
                fun `should save new admin`() {
                    // given
                    val jwt = setupAdmin()

                    val registrationRequest = fixtures.adminRequest

                    // when
                    val adminResponse = mockMvc.post("${fixtures.userUrl}/admin") {
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
                fun `should not save 2 admins with the same email`() {
                    // given one admin is already registered
                    val jwt = setupAdmin()
                    val registrationRequest = fixtures.adminSetupRequest

                    // when
                    val adminResponse = mockMvc.post("${fixtures.userUrl}/admin") {
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
    }

    private fun setupAdmin(): String {
        val adminRequest = fixtures.adminSetupRequest
        userController.createUser(adminRequest)

        val loginRequest = adminRequest.toLoginRequest()

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