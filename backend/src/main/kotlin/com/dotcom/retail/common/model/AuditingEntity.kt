package com.dotcom.retail.common.model

import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@MappedSuperclass
abstract class AuditingEntity {

    @CreationTimestamp
    val createdAt: Instant = Instant.now()

    @UpdateTimestamp
    var updatedAt: Instant = Instant.now()

    override fun toString(): String {
        return "createdAt=$createdAt, updatedAt=$updatedAt"
    }
}