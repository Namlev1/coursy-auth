package com.coursy.masterauthservice.model

import com.coursy.masterauthservice.type.CompanyName
import com.coursy.masterauthservice.type.Email
import com.coursy.masterauthservice.type.Name
import jakarta.persistence.*
import org.hibernate.Hibernate
import java.time.Instant

@Entity(name = "_user")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    var firstName: Name,
    var lastName: Name,
    var email: Email,
    var password: String,
    var companyName: CompanyName?,
    var createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now(),
    var lastLogin: Instant = Instant.now(),
    var enabled: Boolean = true,
    var accountNonLocked: Boolean = false,
    var failedAttempts: Int = 0,
    @ManyToOne
    @JoinColumn(name = "role_id")
    var role: Role
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as User

        return id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}