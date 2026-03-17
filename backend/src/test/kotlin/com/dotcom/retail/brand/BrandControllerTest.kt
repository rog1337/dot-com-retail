package com.dotcom.retail.brand

import com.dotcom.retail.common.BaseIntegrationTest
import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.config.properties.FileProperties
import com.dotcom.retail.domain.catalogue.brand.Brand
import com.dotcom.retail.domain.catalogue.brand.BrandRepository
import com.dotcom.retail.domain.catalogue.brand.BrandService
import com.dotcom.retail.domain.catalogue.brand.CreateBrand
import com.dotcom.retail.domain.catalogue.brand.EditBrand
import com.dotcom.retail.domain.catalogue.image.Image
import com.dotcom.retail.domain.catalogue.image.ImageService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.nio.file.Paths

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class BrandControllerTest : BaseIntegrationTest() {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var brandRepository: BrandRepository
    @MockkBean private lateinit var imageService: ImageService
    @SpykBean private lateinit var brandService: BrandService

    private val dummyImage = Image(
        id = 1,
        fileName = "logo.jpg",
        contentType = "image/jpeg",
        altText = "Brand Logo",
        sortOrder = 0
    )

    @BeforeEach
    fun setup() {
        brandRepository.deleteAll()
    }

    @Test
    fun `get should return 200 and brand data`() {
        val savedBrand = brandRepository.save(Brand(name = "test", isActive = true))

        mockMvc.perform(get("${ApiRoutes.Brand.BASE}/{id}", savedBrand.id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(savedBrand.id))
            .andExpect(jsonPath("$.name").value("test"))
    }

    @Test
    fun `create should return 201 and persist brand`() {
        val request = CreateBrand(
            name = "test",
            isActive = true,
        )

        mockMvc.perform(
            post(ApiRoutes.Brand.BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("test"))

        val brands = brandRepository.findAll()
        assertEquals(1, brands.size)
        assertEquals("test", brands[0].name)
    }

    @Test
    fun `edit should return 200 and update brand details`() {
        val originalBrand = brandRepository.save(Brand(name = "old name", isActive = false))

        val updateRequest = EditBrand(
            id = originalBrand.id,
            name = "new name",
            isActive = true,
        )

        mockMvc.perform(
            put("${ApiRoutes.Brand.BASE}/{id}", originalBrand.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("new name"))

        val updatedBrand = brandRepository.findById(originalBrand.id).get()
        assertEquals("new name", updatedBrand.name)
        assertEquals(true, updatedBrand.isActive)
    }

    @Test
    fun `delete should return 204 and remove brand from DB`() {
        val brand = brandRepository.save(Brand(name = "test"))

        mockMvc.perform(delete("${ApiRoutes.Brand.BASE}/{id}", brand.id))
            .andExpect(status().isNoContent)

        assertFalse(brandRepository.existsById(brand.id))
    }

    @Test
    fun `getImage should return 200 and image resource`() {
        val brandId = 1
        val mockResource = ByteArrayResource("image-data".toByteArray())

        every { brandService.getImage(any()) } returns mockResource

        mockMvc.perform(get("${ApiRoutes.Brand.BASE}/{id}${ApiRoutes.Brand.IMAGE}", brandId))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.IMAGE_JPEG))
            .andExpect(content().bytes(mockResource.byteArray))
    }
}