package com.dotcom.retail.domain.catalogue.product

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query

interface ProductRepository : JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    fun findBySlug(slug: String): Product?

    //    @Query(
//        value = """
//        SELECT value, COUNT(*) AS count
//            FROM (
//            SELECT p.attributes ->> :attrKey AS value
//            FROM product p
//            WHERE p.category_id = :categoryId
//                AND is_active = TRUE
//        ) AS t
//        WHERE value IS NOT NULL
//        GROUP BY value
//    """, nativeQuery = true
//    )
    @Query(
        value = """
    SELECT value, COUNT(*) AS count
    FROM (
        SELECT jsonb_array_elements_text(p.attributes -> :attrKey) AS value
        FROM product p
        WHERE p.category_id = :categoryId
        AND p.is_active = TRUE
        AND (p.attributes -> :attrKey) IS NOT NULL
    ) AS t
    WHERE value IS NOT NULL
    GROUP BY value
    """,
        nativeQuery = true
    )
    fun getAttributeCounts(categoryId: Long, attrKey: String): List<ProductAttributeValueCount>

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
