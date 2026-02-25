package com.dotcom.retail.domain.catalogue.image

import com.dotcom.retail.common.exception.AppException
import com.dotcom.retail.common.exception.ImageError
import com.dotcom.retail.config.properties.FileProperties
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID

@Service
class ImageService(
    private val imageRepository: ImageRepository,
    private val fileProperties: FileProperties,
) {
    companion object {
        const val CONTENT_TYPE_IMAGE_PREFIX = "image/"
    }

    private val logger = LoggerFactory.getLogger(ImageService::class.java)

    fun get(id: Long): Image {
        return imageRepository.findById(id).orElseThrow { AppException(ImageError.IMAGE_NOT_FOUND.withIdentifier(id)) }
    }

    fun getAllById(ids: Set<Long>): List<Image> {
        val images = imageRepository.findAllById(ids)
        if (images.size != ids.size) {
            throw AppException(ImageError.IMAGE_NOT_FOUND.withIdentifier(ids.subtract(images.map { it.id }.toSet())))
        }
        return images
    }

    fun findAllById(ids: List<Long>): List<Image> {
        return imageRepository.findAllById(ids)
    }

    fun save(image: Image): Image {
        return imageRepository.save(image)
    }

    fun isExist(id: Long): Boolean {
        return imageRepository.existsById(id)
    }

    @Transactional
    fun create(imageFile: MultipartFile, metaData: ImageMetadata, directory: Path): Image {

        val uniqueName = UUID.randomUUID().toString() + metaData.fileName

        var image = Image(
            fileName = uniqueName,
            sortOrder = metaData.sortOrder,
            contentType = MediaType.IMAGE_JPEG_VALUE
        )

        image = imageRepository.save(image)
        val filePath = fileProperties.imagesPath.resolve(directory).resolve(uniqueName)
        write(imageFile, filePath)

        return image
    }

    fun write(file: MultipartFile, pathToFile: Path): Boolean {
        if (file.isEmpty) throw AppException(ImageError.IMAGE_EMPTY)

        val contentType = file.contentType ?: throw AppException(ImageError.NOT_AN_IMAGE)
        if (!contentType.startsWith(CONTENT_TYPE_IMAGE_PREFIX)) {
            throw AppException(ImageError.NOT_AN_IMAGE)
        }

        Files.copy(file.inputStream, pathToFile)
        logger.info("Wrote image to $pathToFile")

        return true
    }

    fun findFile(fileName: Path): Resource? {
        val fullPath = fileProperties.imagesPath.resolve(fileName)
        val resource: Resource = UrlResource(fullPath.toUri())
        if (!resource.exists() || !resource.isReadable) return null
        return resource;
    }

    fun getActiveBrandImagePath(brandId: Long): String {
        return imageRepository.findActiveBrandImagePath(brandId) ?: throw AppException(ImageError.IMAGE_NOT_FOUND)
    }

    fun edit(data: EditImage): Image {
        val image = get(data.id)
        image.sortOrder = data.sortOrder
        return save(image)
    }

    fun deleteFile(filePath: Path): Boolean {
        try {
            if (Files.notExists(filePath)) {
                logger.warn("Image file does not exist: $filePath")
                return false
            }

            Files.delete(filePath)
            logger.info("Deleted image file: $filePath")
            return true
        } catch (e: Exception) {
            logger.error("Error deleting image file: $filePath", e)
            return false
        }
    }
}