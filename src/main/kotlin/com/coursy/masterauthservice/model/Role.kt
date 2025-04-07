package com.coursy.masterauthservice.model

import jakarta.persistence.*
import org.hibernate.Hibernate

@Entity
class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,
    @Column(unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    var name: RoleName
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Role

        return id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}