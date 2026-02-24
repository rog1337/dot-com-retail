package com.dotcom.retail.domain.payment

import com.dotcom.retail.common.BaseEntity
import com.dotcom.retail.domain.order.Order
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "transactions")
class Transaction (

    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var type: TransactionType,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: TransactionStatus = TransactionStatus.PENDING,

    @Column(nullable = false)
    var amount: BigDecimal,

    @Column(nullable = false)
    var externalId: String,

    var errorMessage: String? = null
) : BaseEntity()

enum class TransactionType { CHARGE, REFUND }
enum class TransactionStatus { PENDING, SUCCESS, FAILED }