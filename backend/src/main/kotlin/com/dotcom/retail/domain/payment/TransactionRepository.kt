package com.dotcom.retail.domain.payment

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TransactionRepository : JpaRepository<Transaction, UUID> {
    fun findByExternalId(externalId: String): Transaction?
}