package com.coursy.masterauthservice.controller

import com.coursy.masterauthservice.dto.JwtResponse
import com.coursy.masterauthservice.dto.LoginRequest
import com.coursy.masterauthservice.dto.RegistrationRequest
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
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

    private val userUrl = "/v1/user"
    private val authUrl = "/v1/auth"

    @Nested
    inner class `User registration` {

        @Nested
        inner class `When registering regular user` {

            @Nested
            inner class `with correct data` {

                @Test
                fun `should save the user`() {
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
                    val response = mockMvc.post(userUrl) {
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
                    val registrationRequest = RegistrationRequest(
                        firstName = "Jan",
                        lastName = "Kowalski",
                        email = "jan.kowalski@example.com",
                        password = "SecurePassword123!",
                        companyName = null,
                        roleName = null
                    )

                    // when
                    val firstResponse = mockMvc.post(userUrl) {
                        contentType = MediaType.APPLICATION_JSON
                        content = mapper.writeValueAsString(registrationRequest)
                    }

                    val secondResponse = mockMvc.post(userUrl) {
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
        }

        @Nested
        inner class `When registering admin` {

            @Nested
            inner class `with correct data` {
                @Test
                fun `should save new admin`() {
                    // given
                    val adminSetup = setupAdmin()
                    val jwt = adminSetup.jwt
                    val adminRequest = adminSetup.adminRequest

                    val registrationRequest = adminRequest.copy(email = "second_admin@admin.com")

                    // when
                    val adminResponse = mockMvc.post("$userUrl/admin") {
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
    }

    private fun setupAdmin(): AdminSetup {
        val adminRequest = RegistrationRequest(
            firstName = "AdminName",
            lastName = "AdminPassword",
            email = "admin@admin.com",
            password = "SecurePassword123!",
            companyName = null,
            roleName = RoleName.ROLE_ADMIN.toString()
        )
        userController.createUser(adminRequest)

        val loginRequest = LoginRequest(
            email = adminRequest.email,
            password = adminRequest.password
        )

        val authResponse = mockMvc.perform(
            post("$authUrl/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk())
            .andReturn()

        val jwtResponse = mapper.readValue(
            authResponse.response.getContentAsString(StandardCharsets.UTF_8),
            JwtResponse::class.java
        )

        val jwt = jwtResponse.token

        return AdminSetup(adminRequest, jwt)
    }

    // Data class to hold admin setup information
    data class AdminSetup(val adminRequest: RegistrationRequest, val jwt: String)
}