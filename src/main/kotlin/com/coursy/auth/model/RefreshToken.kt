package com.coursy.auth.model

import jakarta.persistence.*
import java.time.Instant

@Entity
class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    var user: User,

    @Column(nullable = false, unique = true)
    var token: String,

    @Column(nullable = false)
    var expiryDate: Instant
)
