package com.dotcom.retail.domain.catalogue.image

import com.dotcom.retail.common.exception.AppException
import com.dotcom.retail.common.exception.ImageError
import com.dotcom.retail.config.properties.FileProperties
import net.coobird.thumbnailator.Thumbnails
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.imageio.ImageIO

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

    @Transactional
    fun create(multipartFile: MultipartFile, metaData: ImageMetadata, directory: Path): Image {
        val imageFile = ImageIO.read(multipartFile.inputStream)
            ?: throw AppException(ImageError.CANNOT_READ_IMAGE.withIdentifier(multipartFile.name))

        val contentType = multipartFile.contentType ?: MediaType.IMAGE_JPEG_VALUE
        return create(imageFile, contentType, metaData, directory)
    }

    @Transactional
    fun create(imageFile: BufferedImage, contentType: String, metaData: ImageMetadata, directory: Path): Image {
        val baseName = UUID.randomUUID().toString().replace("-", "")
        val contentType = contentType

        val image = Image(
            fileName = baseName,
            contentType = contentType,
            sortOrder = metaData.sortOrder,
            altText = metaData.altText,
        )

        ImageSize.entries.forEach { size ->
            val dest = fileProperties.imagesPathFull.resolve(directory).resolve(image.fileNameForSize(size))

            Thumbnails.of(imageFile)
                .size(size.width, size.height)
                .keepAspectRatio(true)
                .toFile(dest.toFile())

            logger.debug("Saved {} → {}", size, dest)
        }

        return imageRepository.save(image)
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