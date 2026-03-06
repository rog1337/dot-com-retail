package com.dotcom.retail.domain.cart

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.Optional
import java.util.UUID

interface CartRepository : JpaRepository<Cart, UUID> {
    @EntityGraph(attributePaths = ["items"])
    fun findByUserId(userId: UUID): Cart?

    @EntityGraph(attributePaths = ["items"])
    fun findBySessionId(sessionId: String): Cart?

    @EntityGraph(attributePaths = ["items"])
    override fun findById(id: UUID): Optional<Cart?>

    fun findByIntentId(intentId: String): Cart?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cart c WHERE c.sessionId = :sessionId")
    fun lockBySessionId(sessionId: String): Cart?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId")
    fun lockByUserId(userId: UUID): Cart?
}