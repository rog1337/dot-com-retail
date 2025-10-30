package com.dotcom.retail.catalogue.product

import com.dotcom.retail.catalogue.brand.Brand
import com.dotcom.retail.catalogue.category.Category
import com.dotcom.retail.catalogue.dimension.Dimension
import com.dotcom.retail.catalogue.image.ProductImage
import com.dotcom.retail.util.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(indexes = [
    Index(columnList = "name"),
    Index(columnList = "id"),
    Index(columnList = "category_id"),
])
class Product(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var productId: String? = null,
    var name: String,
    var slug: String,

    var productDescription: String? = null,
    var storeDescription: String? = null,

    var price: BigDecimal? = BigDecimal.ZERO,

    var stock: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    var brand: Brand? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: Category? = null,

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    var images: MutableList<ProductImage> = mutableListOf(),

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    var attributes: MutableList<ProductAttribute> = mutableListOf(),

    @Embedded
    var dimensions: Dimension? = null,

    var weightKg: Double? = null,
    var weightLbs: Double? = null,

    var listed: Boolean = false,

    ) : BaseEntity()