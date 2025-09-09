package com.coursy.auth.model

import arrow.core.Either
import org.springframework.security.core.GrantedAuthority

enum class Role {
    ROLE_HOST_OWNER,
    ROLE_HOST_ADMIN,
    ROLE_TENANT,
    ROLE_PLATFORM_OWNER,
    ROLE_PLATFORM_ADMIN,
    ROLE_PLATFORM_USER
}

fun GrantedAuthority.toRole(): Either<Throwable, Role> {
    val roleString = this.authority.removePrefix("ROLE_")
    return Either.catch { Role.valueOf(roleString) }
}
