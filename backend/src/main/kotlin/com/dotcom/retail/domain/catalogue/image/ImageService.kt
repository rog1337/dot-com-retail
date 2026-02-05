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
import java.io.File
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
    private val productFilePath = Paths.get(fileProperties.image.product.dir)
    private val brandFilePath = Paths.get(fileProperties.image.brand.dir)

    init {
        val pathList = listOf(productFilePath, brandFilePath)
        for (path in pathList) {
            val file = File(path.toString())
            if (!file.exists()) {
                try {
                    file.mkdirs()
                } catch (e: Exception) {
                    logger.error("Error creating directory $path, ${e.printStackTrace()}")
                }
                logger.info("Created upload directory ${file.absolutePath}")
            }
        }
    }

    fun get(id: Long): Image {
        return imageRepository.findById(id).orElseThrow { NotFoundException(Image::class.simpleName, id) }
    }

//    fun getByName(name: String): Image {
//        return imageRepository.findByName(name) ?: throw ImageNotFoundException(name)
//    }

    fun findAllById(ids: List<Long>): List<Image> {
        return imageRepository.findAllById(ids)
    }

    fun save(image: Image): Image {
        return imageRepository.save(image)
    }

    fun isExist(id: Long): Boolean {
        return imageRepository.existsById(id)
    }

//    fun createProductImage(imageFile: MultipartFile, data: CreateImage): Image {
//        val path = write(imageFile, productFilePath).toString()
//        val image = create(path, MediaType.IMAGE_JPEG, data.sortOrder)
//        return save(image)
//    }
//
//    fun createBrandImage(imageFile: MultipartFile, data: CreateImage = CreateImage()): Image {
//        val path = write(imageFile, brandFilePath).toString()
//        val image = create(path, MediaType.IMAGE_JPEG, data.sortOrder)
//        return save(image)
//    }

    @Transactional
    fun create(imageFile: MultipartFile, metaData: ImageMetadata, directory: Path): Image {

        val uniqueName = UUID.randomUUID().toString() + metaData.fileName
        val filePath = directory.resolve(uniqueName)

        var image = Image(
            filePath = filePath.toString(),
            sortOrder = metaData.sortOrder,
            contentType = MediaType.IMAGE_JPEG_VALUE
        )

        image = imageRepository.save(image)
        write(imageFile, filePath)

        return image
    }

//    fun create(filePath: String, contentType: MediaType = MediaType.IMAGE_JPEG, sortOrder: Int = 0): Image {
//        val image = Image(
//            filePath = filePath,
//            contentType = contentType.toString(),
//            sortOrder = sortOrder,
//        )
//        return save(image)
//    }

    fun write(file: MultipartFile, pathToFile: Path): Boolean {
        if (file.isEmpty) throw ImageIsEmptyException(file)

        val contentType = file.contentType ?: throw FileIsNotAnImageException(file)
        if (!contentType.startsWith(CONTENT_TYPE_IMAGE_PREFIX)) {
            throw FileIsNotAnImageException(file)
        }

//        val uniqueName = "${UUID.randomUUID()}.${file.originalFilename}"
//        val resolve = path.resolve(uniqueName)

        Files.copy(file.inputStream, pathToFile)
        logger.info("Wrote image to $pathToFile")

        return true
    }

    fun findFile(path: String): Resource? {
        val resource: Resource = UrlResource(Paths.get(path).toUri())
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