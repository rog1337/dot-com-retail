package com.dotcom.retail.domain.catalogue.brand

import com.dotcom.retail.common.exception.BrandNotFoundException
import com.dotcom.retail.common.exception.ImageNotFoundException
import com.dotcom.retail.domain.catalogue.image.ImageDeletionEvent
import com.dotcom.retail.domain.catalogue.image.ImageService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.io.Resource
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BrandService(
    private val brandRepository: BrandRepository,
    private val imageService: ImageService,
    private val eventPublisher: ApplicationEventPublisher
) {

    fun find(id: Long): Brand? {
        return brandRepository.findByIdOrNull(id)
    }

    fun get(id: Long): Brand {
        return brandRepository.findById(id).orElseThrow { BrandNotFoundException(id) }
    }

    fun save(brand: Brand): Brand {
        return brandRepository.save(brand)
    }

    fun isExist(id: Long): Boolean {
        return brandRepository.existsById(id)
    }

    @Transactional
    fun create(data: CreateBrand): Brand {
        val image = data.image.let(imageService::get)
        val brand = Brand(name = data.name, image = image)
        return save(brand)
    }

    @Transactional
    fun edit(data: EditBrand): Brand {
        val brand = get(data.id)

        data.image?.let { newImageId ->
            val newImage = imageService.get(newImageId)
            val currentImage = brand.image

            if (currentImage != null && currentImage.id != newImage.id) {
                eventPublisher.publishEvent(ImageDeletionEvent(listOf(currentImage.filePath)))
            }

            brand.image = newImage
        }

        brand.apply {
            name = data.name
            isActive = data.isActive
        }

        return save(brand)
    }

    @Transactional
    fun delete(id: Long) {
        val brand = get(id)
        val brandImagePath = brand.image?.filePath

        if (brandImagePath != null) {
            eventPublisher.publishEvent(ImageDeletionEvent(listOf(brandImagePath)))
        }

        brandRepository.delete(brand)
    }

    fun getImage(id: Long): Resource {
        val imagePath = imageService.getActiveBrandImagePath(id)
        val imageFile = imageService.findFile(imagePath) ?: throw ImageNotFoundException()
        return imageFile
    }
}