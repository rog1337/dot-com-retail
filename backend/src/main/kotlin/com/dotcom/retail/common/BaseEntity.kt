package com.dotcom.retail.common

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@MappedSuperclass
abstract class BaseEntity {

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    open val createdAt: LocalDateTime? = null

    @UpdateTimestamp
    @Column(nullable = false)
    open var updatedAt: LocalDateTime? = null

    override fun toString(): String {
        val fields: Map<String, Any?> = mapOf(
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
        )

        return "${this::class.simpleName.toString()}()${formatToString(fields)}"
    }

    fun formatToString(fields: Map<String, Any?>): String {
       return fields.entries.joinToString(" ") { entry -> " ${entry.key}=${entry.value?.toString() ?: "null"}" }
    }
}