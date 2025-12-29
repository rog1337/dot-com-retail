package com.dotcom.retail.domain.catalogue.image

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ImageRepository : JpaRepository<Image, Long> {

    @Query("""
            SELECT i.filePath FROM Product p
            JOIN p.images i 
            WHERE p.id = :productId
                AND i.id = :imageId
                AND p.isActive = true
        """)
    fun findActiveProductImagePath(productId: Long, imageId: Long): String?

    @Query("""
        SELECT i.filePath FROM Brand b 
        JOIN b.image i 
        WHERE b.id = :brandId
            AND b.isActive = true
    """)
    fun findActiveBrandImagePath(brandId: Long): String?
}