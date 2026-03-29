package com.dotcom.retail.domain.admin.product

import com.dotcom.retail.domain.admin.product.dto.AdminProductDto
import com.dotcom.retail.domain.catalogue.product.Product
import com.dotcom.retail.domain.catalogue.product.ProductRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class AdminProductService(
    private val productRepository: ProductRepository
) {

    fun getProducts(page: Int, pageSize: Int): Page<Product> {
        val pageable = PageRequest.of(page, pageSize)
        val products = productRepository.findAllAdmin(pageable)
        return products

    }

    fun getProductsByText(query: String, page: Int, pageSize: Int): Page<Product> {
        val pageable = PageRequest.of(page, pageSize)
        return productRepository.searchByText(query, pageable)
    }
}