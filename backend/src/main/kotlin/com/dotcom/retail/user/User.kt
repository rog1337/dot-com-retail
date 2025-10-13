package com.dotcom.retail.user

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.util.UUID

@Entity
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID,
    val email: String,
    val password: String,
)
