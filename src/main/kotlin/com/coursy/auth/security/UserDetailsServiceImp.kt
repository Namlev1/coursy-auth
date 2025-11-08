package com.coursy.auth.security

import com.coursy.auth.repository.UserRepository
import com.coursy.auth.repository.UserSpecification
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserDetailsServiceImp(
    private val userRepository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val parts = username.split("::")
        if (parts.size != 2) {
            throw UsernameNotFoundException("Invalid username format")
        }

        val email = parts[0]
        val platformId = if (parts[1] == "HOST_PLATFORM") {
            null
        } else {
            runCatching { UUID.fromString(parts[1]) }
                .getOrElse { throw UsernameNotFoundException("Invalid platform ID format") }
        }

        val spec = UserSpecification.builder()
            .email(email)
            .platformId(platformId)
            .build()
        val user = userRepository.findOne(spec).orElse(null)
        return user?.toUserDetails() ?: throw UsernameNotFoundException("User not found")
    }
}