package com.dotcom.retail.domain.catalogue.product

import com.dotcom.retail.domain.catalogue.filter.ValueCount
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import java.math.BigDecimal

interface ProductRepository : JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Query("""
    SELECT p.* FROM product p
    LEFT JOIN brand b ON p.brand_id = b.id
    WHERE p.is_active = true
      AND (
          p.search_content ILIKE ALL(
              SELECT '%' || word || '%' 
              FROM unnest(string_to_array(lower(trim(:query)), ' ')) AS word
          )
          OR 
          b.name ILIKE ALL(
              SELECT '%' || word || '%' 
              FROM unnest(string_to_array(lower(trim(:query)), ' ')) AS word
          )
      )
    ORDER BY 
        similarity(p.search_content, lower(:query)) DESC,
        similarity(coalesce(b.name, ''), :query) DESC
""",
        countQuery = """
    SELECT count(*) FROM product p
    LEFT JOIN brand b ON p.brand_id = b.id
    WHERE p.is_active = true
      AND (
          p.search_content ILIKE ALL(
              SELECT '%' || word || '%' 
              FROM unnest(string_to_array(lower(trim(:query)), ' ')) AS word
          )
          OR 
          b.name ILIKE ALL(
              SELECT '%' || word || '%' 
              FROM unnest(string_to_array(lower(trim(:query)), ' ')) AS word
          )
      )
""", nativeQuery = true)
    fun searchByText(query: String, pageable: Pageable): Page<Product>

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

    @Query("""
        SELECT MIN(p.price) as min, MAX(p.price) as max 
        FROM Product p 
        WHERE p.isActive = true
    """)
    fun findPriceRange(categoryId: Long): PriceRange
}

data class PriceRange(
    val min: BigDecimal = BigDecimal.ZERO,
    val max: BigDecimal = BigDecimal.ZERO
)