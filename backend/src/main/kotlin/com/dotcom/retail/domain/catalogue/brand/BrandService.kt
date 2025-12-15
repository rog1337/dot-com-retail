package com.dotcom.retail.domain.catalogue.brand

import com.dotcom.retail.common.exception.BrandNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class BrandService(private val brandRepository: BrandRepository) {

    fun find(id: Long): Brand? {
        return brandRepository.findByIdOrNull(id)
    }

    fun get(id: Long): Brand {
        return brandRepository.findById(id).orElseThrow { BrandNotFoundException(id) }
    }
}