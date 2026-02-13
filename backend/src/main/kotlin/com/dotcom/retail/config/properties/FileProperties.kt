package com.dotcom.retail.config.properties

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

@ConfigurationProperties(prefix = "file")
data class FileProperties (
    val root: String,
    val structure: Structure,
) {
    private val logger: Logger = LoggerFactory.getLogger(FileProperties::class.java)

    val rootPath: Path = Paths.get(root).toAbsolutePath().normalize()
    val imagesPath: Path = rootPath.resolve(structure.images)
    val productPath: Path = Paths.get(structure.products)
    val productPathFull: Path = imagesPath.resolve(structure.products)
    val brandPath: Path = Paths.get(structure.brands)
    val brandPathFull: Path = imagesPath.resolve(structure.brands)

    init {
        val pathList = listOf(productPathFull, brandPathFull)
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

    data class Structure(
        val images: String = "images",
        val products: String = "products",
        val brands: String = "brands"
    )
}