package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.domain.catalogue.brand.Brand
import com.dotcom.retail.domain.catalogue.category.Category
import com.dotcom.retail.domain.catalogue.image.Image
import com.dotcom.retail.common.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
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

    var name: String,
    var sku: String,
    var slug: String,

    var description: String? = null,

    var price: BigDecimal = BigDecimal.ZERO,
    var salePrice: BigDecimal = BigDecimal.ZERO,

    var stock: Int = 0,

    @ManyToOne
    @JoinColumn(name = "brand_id")
    var brand: Brand? = null,

    @ManyToOne
    @JoinColumn(name = "category_id")
    var category: Category? = null,

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
//    @JoinTable(
//        name = "product_images",
//        joinColumns = [JoinColumn(name = "product_id")],
//        inverseJoinColumns = [JoinColumn(name = "image_id")]
//    )
    var images: MutableList<Image> = mutableListOf(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var attributes: Map<String, Any>? = null,

    var isActive: Boolean = false

) : BaseEntity() {
    override fun toString(): String {
        return "Product(id=$id, name='$name', sku='$sku', slug='$slug', description=$description, price=$price, salePrice=$salePrice, stock=$stock, brand=$brand, category=$category, images=$images, attributes=$attributes, isActive=$isActive, ${super.toString()})"
    }
}