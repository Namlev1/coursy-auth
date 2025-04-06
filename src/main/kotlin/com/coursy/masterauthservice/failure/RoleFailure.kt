package com.coursy.masterauthservice.failure

sealed class RoleFailure : Failure {
    data object NotFound : RoleFailure()

    override fun message(): String = when (this) {
        NotFound -> "Role not found"
    }
}