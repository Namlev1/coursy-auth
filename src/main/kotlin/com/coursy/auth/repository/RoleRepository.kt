package com.coursy.auth.repository

import com.coursy.auth.model.Role
import com.coursy.auth.model.RoleName
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository : JpaRepository<Role, Long> {
    fun findByName(name: RoleName): Role?
} 