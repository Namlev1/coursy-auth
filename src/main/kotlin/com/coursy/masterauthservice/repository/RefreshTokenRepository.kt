package com.coursy.masterauthservice.repository

import com.coursy.masterauthservice.model.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> 