package com.coursy.masterauthservice.security

import com.coursy.masterauthservice.model.User
import com.coursy.masterauthservice.type.CompanyName
import com.coursy.masterauthservice.type.Email
import com.coursy.masterauthservice.type.Name
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserDetailsImp(
    val id: Long,
    val email: Email,
    private val authorities: MutableCollection<SimpleGrantedAuthority>,
    private val password: String,
    val firstName: Name,
    val lastName: Name,
    val companyName: CompanyName?,
    private val enabled: Boolean,
    private val accountNonLocked: Boolean

) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getPassword() = password

    override fun getUsername() = email.value

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = accountNonLocked

    override fun isCredentialsNonExpired() = true

    override fun isEnabled() = enabled

}

fun User.toUserDetails(): UserDetailsImp {
    val authorities = mutableSetOf(SimpleGrantedAuthority(this.role.name.name))

    return UserDetailsImp(
        id = this.id,
        email = this.email,
        password = password,
        firstName = this.firstName,
        lastName = this.lastName,
        companyName = this.companyName,
        authorities = authorities,
        enabled = this.enabled,
        accountNonLocked = this.accountNonLocked
    )
}
