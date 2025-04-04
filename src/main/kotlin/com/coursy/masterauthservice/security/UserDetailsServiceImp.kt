package com.coursy.masterauthservice.security

import com.coursy.masterauthservice.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImp(
    private val userRepository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmailAndFetchRoles(username)
        return user?.toUserDetails() ?: throw UsernameNotFoundException("User with username $username was not found")
    }
}