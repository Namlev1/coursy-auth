package com.coursy.masterauthservice.repository

import com.coursy.masterauthservice.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    @Query("SELECT u from _user u JOIN FETCH u.roles WHERE u.email = :email")
    fun findByEmailAndFetchRoles(@Param("email") email: String): User?
}