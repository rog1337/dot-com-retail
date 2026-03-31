package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.common.model.AuditingEntity
import com.dotcom.retail.domain.catalogue.brand.Brand
import com.dotcom.retail.domain.catalogue.category.Category
import com.dotcom.retail.domain.catalogue.image.Image
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal

@Entity
class Product(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var name: String,
    var sku: String,

    var description: String? = null,

    var price: BigDecimal = BigDecimal.ZERO,
    var salePrice: BigDecimal = BigDecimal.ZERO,

    var stock: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    @JsonManagedReference
    var brand: Brand? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: Category? = null,

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    var images: MutableList<Image> = mutableListOf(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var attributes: MutableMap<String, MutableList<Any>>? = mutableMapOf(),

    var reviewCount: Int = 0,
    var averageRating: Double = 0.0,

    var isActive: Boolean = false,

    @JsonIgnore
    var searchContent: String? = null,

    @Version
    var version: Long? = null

) : AuditingEntity() {

    @PrePersist
    @PreUpdate
    fun updateSearchContent() {
        val attributeText = attributes?.values?.flatten()?.joinToString(" ") { it.toString() } ?: ""

        this.searchContent = """
            $name 
            $sku 
            ${description ?: ""} 
            $attributeText
        """.trimIndent().lowercase().replace("\\s+".toRegex(), " ")
    }

    override fun toString(): String {
        return "Product(id=$id, name='$name', sku='$sku', description=$description, price=$price, salePrice=$salePrice, stock=$stock, attributes=$attributes, isActive=$isActive, ${super.toString()})"
    }
}
