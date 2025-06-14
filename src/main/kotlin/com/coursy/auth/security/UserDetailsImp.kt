package com.coursy.auth.security

import com.coursy.auth.model.User
import com.coursy.auth.type.Email
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserDetailsImp(
    val id: Long,
    val email: Email,
    private val password: String,
    private val enabled: Boolean,
    private val accountNonLocked: Boolean,
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> = emptyList()

    override fun getPassword() = password

    override fun getUsername() = email.value

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = accountNonLocked

    override fun isCredentialsNonExpired() = true

    override fun isEnabled() = enabled

}

fun User.toUserDetails(): UserDetailsImp {

    return UserDetailsImp(
        id = this.id,
        email = this.email,
        password = password,
        enabled = this.enabled,
        accountNonLocked = this.accountNonLocked
    )
}
