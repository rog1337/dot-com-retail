package com.dotcom.retail.domain.admin.brand

import com.dotcom.retail.domain.catalogue.brand.Brand
import com.dotcom.retail.domain.catalogue.brand.BrandRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class AdminBrandService(private val brandRepository: BrandRepository) {
    fun search(query: String, page: Int, size: Int): Page<Brand> {
        val pageable = PageRequest.of(page, size)
        return brandRepository.findByNameContainingIgnoreCase(query, pageable)
    }
}