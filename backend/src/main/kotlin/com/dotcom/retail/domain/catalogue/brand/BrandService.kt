package com.dotcom.retail.domain.catalogue.brand

import com.dotcom.retail.common.exception.NotFoundException
import com.dotcom.retail.config.properties.FileProperties
import com.dotcom.retail.domain.catalogue.image.Image
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
    private val eventPublisher: ApplicationEventPublisher,
    private val fileProperties: FileProperties
) {

    fun find(id: Long): Brand? {
        return brandRepository.findByIdOrNull(id)
    }

    fun get(id: Long): Brand {
        return brandRepository.findById(id).orElseThrow { NotFoundException(Brand::class.simpleName, id) }
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
                val imagePath = fileProperties.brandPathFull.resolve(currentImage.fileName)
                eventPublisher.publishEvent(ImageDeletionEvent(listOf(imagePath.toString())))
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
        val brandImageName = brand.image?.fileName

        if (brandImageName != null) {
            val imagePath = fileProperties.brandPathFull.resolve(brandImageName)
            eventPublisher.publishEvent(ImageDeletionEvent(listOf(imagePath.toString())))
        }

        brandRepository.delete(brand)
    }

    fun getImage(id: Long): Resource {
        val imageFileName = imageService.getActiveBrandImagePath(id)
        val imagePath = fileProperties.brandPath.resolve(imageFileName)
        val imageFile = imageService.findFile(imagePath) ?: throw NotFoundException(Image::class.simpleName, id)
        return imageFile
    }
}