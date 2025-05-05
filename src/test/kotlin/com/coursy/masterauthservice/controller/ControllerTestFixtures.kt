package com.coursy.masterauthservice.controller

import com.coursy.masterauthservice.dto.LoginRequest
import com.coursy.masterauthservice.dto.RegistrationRequest
import com.coursy.masterauthservice.model.RoleName

class ControllerTestFixtures {
    // API endpoints
    val userUrl = "/v1/user"
    val adminUrl = "$userUrl/admin"
    val superAdminUrl = "$userUrl/super-admin"
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

    val userRoleName = RoleName.ROLE_USER.toString()
    val adminRoleName = RoleName.ROLE_ADMIN.toString()
    val superAdminRoleName = RoleName.ROLE_SUPER_ADMIN.toString()

    val regularUserRequest = RegistrationRequest(
        firstName = regularName,
        lastName = regularLastName,
        email = regularEmail,
        password = regularPassword,
        companyName = null,
        roleName = null
    )

    val adminRequest = RegistrationRequest(
        firstName = regularName,
        lastName = regularLastName,
        email = regularEmail,
        password = regularPassword,
        companyName = null,
        roleName = adminRoleName
    )

    val superAdminRequest = RegistrationRequest(
        firstName = regularName,
        lastName = regularLastName,
        email = regularEmail,
        password = regularPassword,
        companyName = null,
        roleName = superAdminRoleName
    )

    val adminSetupRequest = RegistrationRequest(
        firstName = regularName + "Setup",
        lastName = regularLastName + "Setup",
        email = adminSetupEmail,
        password = regularPassword,
        companyName = null,
        roleName = adminRoleName
    )

    val superAdminSetupRequest = RegistrationRequest(
        firstName = regularName + "SuperAdminSetup",
        lastName = regularLastName + "SuperAdminSetup",
        email = superAdminSetupEmail,
        password = regularPassword,
        companyName = null,
        roleName = superAdminRoleName
    )

    fun createRegistrationRequest(
        firstName: String = unusedName,
        lastName: String = unusedLastName,
        email: String = unusedEmail,
        password: String = unusedPassword,
        companyName: String? = null,
        role: String = userRoleName
    ) = RegistrationRequest(
        firstName = firstName,
        lastName = lastName,
        email = email,
        password = password,
        companyName = companyName,
        roleName = role
    )

}

fun RegistrationRequest.toLoginRequest() = LoginRequest(
    this.email,
    this.password
)
