package com.coursy.masterauthservice.repository

import com.coursy.masterauthservice.model.RefreshToken
import com.coursy.masterauthservice.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun deleteByUser(user: User): Unit
} 