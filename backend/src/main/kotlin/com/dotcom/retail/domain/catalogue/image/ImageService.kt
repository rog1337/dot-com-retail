package com.dotcom.retail.domain.catalogue.image

import com.dotcom.retail.common.exception.FileIsNotAnImageException
import com.dotcom.retail.common.exception.ImageIsEmptyException
import com.dotcom.retail.common.exception.NotFoundException
import com.dotcom.retail.config.properties.FileProperties
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
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
        return imageRepository.findById(id).orElseThrow { NotFoundException(Image::class.simpleName, id) }
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
        val filePath = directory.resolve(uniqueName)

        var image = Image(
            fileName = filePath.toString(),
            sortOrder = metaData.sortOrder,
            contentType = MediaType.IMAGE_JPEG_VALUE
        )

        image = imageRepository.save(image)
        write(imageFile, filePath)

        return image
    }

    fun write(file: MultipartFile, pathToFile: Path): Boolean {
        if (file.isEmpty) throw ImageIsEmptyException(file)

        val contentType = file.contentType ?: throw FileIsNotAnImageException(file)
        if (!contentType.startsWith(CONTENT_TYPE_IMAGE_PREFIX)) {
            throw FileIsNotAnImageException(file)
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

    fun getActiveProductImagePath(productId: Long, imageId: Long): String {
        return imageRepository.findActiveProductImagePath(productId, imageId) ?: throw NotFoundException(Image::class.simpleName, imageId)
    }

    fun getActiveBrandImagePath(brandId: Long): String {
        return imageRepository.findActiveBrandImagePath(brandId) ?: throw NotFoundException(Image::class.simpleName)
    }

    fun edit(data: EditImage): Image {
        val image = get(data.id)
        image.sortOrder = data.sortOrder
        return save(image)
    }

    fun deleteFile(filePath: String): Boolean {
        try {
            if (Files.notExists(Paths.get(filePath))) {
                logger.warn("Image file does not exist: $filePath")
                return false
            }

            Files.delete(Paths.get(filePath))
            logger.info("Deleted image file: $filePath")
            return true
        } catch (e: Exception) {
            logger.error("Error deleting image file: $filePath", e)
            return false
        }
    }
}