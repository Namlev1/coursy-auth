package com.coursy.masterauthservice.failure

sealed class RoleFailure {
    data class NotFound(val name: String) : RoleFailure()
}