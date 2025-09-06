package com.coursy.auth.failure

sealed class UserFailure : Failure {
    data object IdNotExists : UserFailure()
    data object IdExists : UserFailure()

    override fun message(): String = when (this) {
        IdNotExists -> "User with this id does not exist."
        IdExists -> "User with this id already exists."
    }
}