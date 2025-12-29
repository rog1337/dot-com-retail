package com.dotcom.retail.domain.catalogue.image

import com.dotcom.retail.common.constants.ApiRoutes.Image
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(Image.BASE)
class ImageController(
    private val imageService: ImageService
) {

    @PutMapping("{id}")
    fun edit(@RequestBody data: EditImage): ResponseEntity<ImageDto> {
        val image = imageService.edit(data)
        return ResponseEntity.ok(image.toDto())
    }

    @PostMapping(Image.PRODUCT, consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createProductImage(
        @RequestPart("image") image: MultipartFile,
        @RequestPart("data") data: CreateImage)
    : ResponseEntity<ImageDto> {
        val image = imageService.createProductImage(image, data)
        return ResponseEntity<ImageDto>(image.toDto(), HttpStatus.CREATED)
    }

    @PostMapping(Image.BRAND)
    fun createBrandImage(
        @RequestPart("image") image: MultipartFile,
        @RequestPart("data") data: CreateImage = CreateImage()
        ): ResponseEntity<ImageDto> {
        val image = imageService.createBrandImage(image, data)
        return ResponseEntity<ImageDto>(image.toDto(), HttpStatus.CREATED)
    }
}