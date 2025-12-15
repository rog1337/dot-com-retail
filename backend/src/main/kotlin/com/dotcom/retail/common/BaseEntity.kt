package com.dotcom.retail.common

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@MappedSuperclass
abstract class BaseEntity {

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()

    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()

    override fun toString(): String {
        return "createdAt=$createdAt, updatedAt=$updatedAt"
    }
}