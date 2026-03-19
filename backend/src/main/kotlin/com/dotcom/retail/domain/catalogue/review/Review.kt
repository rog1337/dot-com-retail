package com.dotcom.retail.domain.catalogue.review

import com.dotcom.retail.common.model.AuditingEntity
import com.dotcom.retail.domain.catalogue.product.Product
import com.dotcom.retail.domain.user.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.BatchSize

@Entity
@Table(name = "reviews")
class Review(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    val product: Product,

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    val user: User,

    var rating: Int,
    var body: String? = null,

    @OneToMany(
        mappedBy = "review",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @BatchSize(size = 20)
    var votes: MutableList<ReviewVote> = mutableListOf(),
) : AuditingEntity() {

    override fun toString(): String {
        return "Review(id=$id, rating=$rating, body=$body, ${super.toString()})"
    }
}