package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.domain.catalogue.category.attribute.AttributeDataType
import com.dotcom.retail.domain.catalogue.filter.ValueCount
import jakarta.persistence.Tuple
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query

interface ProductRepository : JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    fun findBySlug(slug: String): Product?

    @Query("""
        SELECT
            elem as value,
            COUNT(p.id) as count
        FROM product p
        CROSS JOIN LATERAL jsonb_array_elements_text(p.attributes -> :attrKey) as elem
        WHERE p.category_id = :categoryId
          AND p.is_active = true
        GROUP BY elem
    """, nativeQuery = true)
    fun findAttributeCounts(categoryId: Long, attrKey: String): List<ValueCount>


    @Query(
        """
        SELECT
            b.id AS id,
            b.name AS name,
            COUNT(*) AS count
        FROM product p
        JOIN brand b ON p.brand_id = b.id
        WHERE p.category_id = :categoryId
          AND p.is_active = TRUE
          AND b.is_active = TRUE
        GROUP BY b.id, b.name;
    """, nativeQuery = true
    )
    fun getBrandCounts(categoryId: Long): List<ProductBrandCount>
}
