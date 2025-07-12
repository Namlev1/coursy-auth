package com.coursy.auth.repository

import com.coursy.auth.model.RefreshToken
import com.coursy.auth.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {
    fun deleteByUser(user: User)
    fun findByToken(token: String): RefreshToken?
} 