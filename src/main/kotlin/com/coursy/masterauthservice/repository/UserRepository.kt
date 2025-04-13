package com.coursy.masterauthservice.repository

import com.coursy.masterauthservice.model.User
import com.coursy.masterauthservice.type.Email
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: Email): Boolean
    fun removeUserById(id: Long)
}