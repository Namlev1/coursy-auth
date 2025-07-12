package com.coursy.auth.model

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
class RefreshToken(
    @Id
    var id: UUID = UUID.randomUUID(),

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    var user: User,

    @Column(nullable = false, unique = true)
    var token: String,

    @Column(nullable = false)
    var expiryDate: Instant
)
