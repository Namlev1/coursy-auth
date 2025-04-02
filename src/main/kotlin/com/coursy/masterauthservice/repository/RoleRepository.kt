package com.coursy.masterauthservice.repository

import com.coursy.masterauthservice.model.Role
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository : JpaRepository<Role, Long> 