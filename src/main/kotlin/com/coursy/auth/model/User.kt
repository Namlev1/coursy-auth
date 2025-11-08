package com.coursy.auth.model

import com.coursy.auth.type.Email
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.Hibernate
import java.time.Instant
import java.util.*

@Entity(name = "_user")
class User(
    @Id
    var id: UUID = UUID.randomUUID(),
    var email: Email,
    var password: String,
    var createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now(),
    var lastLogin: Instant = Instant.now(),
    var enabled: Boolean = true,
    var accountNonLocked: Boolean = true,
    var failedAttempts: Int = 0,
    var platformId: UUID?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as User

        return id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}