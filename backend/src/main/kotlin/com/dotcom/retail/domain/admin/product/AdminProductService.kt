package com.dotcom.retail.domain.admin.product

import com.dotcom.retail.domain.catalogue.product.Product
import com.dotcom.retail.domain.catalogue.product.ProductRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class AdminProductService(
    private val productRepository: ProductRepository
) {

    fun getProducts(page: Int, size: Int): Page<Product> {
        val pageable = PageRequest.of(page, size)
        val products = productRepository.findAllAdmin(pageable)
        return products
    }

    fun getProductsByText(query: String, page: Int, size: Int): Page<Product> {
        val pageable = PageRequest.of(page, size)
        return productRepository.searchByText(query, pageable)
    }
}