package com.coursy.auth.failure

sealed class RefreshTokenFailure : Failure {
    data object Empty : RefreshTokenFailure()
    data object NotFound : RefreshTokenFailure()
    data object Expired : RefreshTokenFailure()

    override fun message(): String = when (this) {
        Empty -> "Refresh token is empty"
        NotFound -> "Refresh token was not found"
        Expired -> "Refresh token has expired"
    }
}
