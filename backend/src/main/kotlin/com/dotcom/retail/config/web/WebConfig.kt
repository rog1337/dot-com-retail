package com.dotcom.retail.config.web

import com.dotcom.retail.common.constants.ApiRoutes.Image
import com.dotcom.retail.config.properties.FileProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val fileProperties: FileProperties,
) : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler(Image.SERVE)
            .addResourceLocations("file:${fileProperties.imagesPathFull}")
    }
}