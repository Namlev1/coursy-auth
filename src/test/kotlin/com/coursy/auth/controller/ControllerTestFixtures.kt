package com.coursy.auth.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.test.web.servlet.MockMvc

@Component
class ControllerTestFixtures(
    private val mapper: ObjectMapper = ObjectMapper(),
    private val mockMvc: MockMvc
) {
    // API endpoints
    val userUrl = "/v1/user"
    val adminUrl = "/v1/admin"
    val superAdminUrl = "/v1/super-admin"
    val authUrl = "/v1/auth"

    // Data fields
    val regularName = "Jan"
    val regularLastName = "Kowalski"
    val regularEmail = "jan.kowalski@example.com"
    val regularPassword = "SecurePassword123!"

    val adminSetupEmail = "admin_setup@email.com"
    val superAdminSetupEmail = "super_admin_setup@email.com"

    val unusedName = "test_name"
    val unusedLastName = "test_last_name"
    val unusedEmail = "test_email@test.com"
    val unusedPassword = "Pa$\$w0RD"
}
