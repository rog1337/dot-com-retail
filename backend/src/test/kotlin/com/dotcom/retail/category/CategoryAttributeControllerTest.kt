package com.dotcom.retail.category

import com.dotcom.retail.common.BaseIntegrationTest
import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.domain.admin.category.dto.CreateCategoryAttribute
import com.dotcom.retail.domain.admin.category.dto.EditCategoryAttribute
import com.dotcom.retail.domain.catalogue.category.Category
import com.dotcom.retail.domain.catalogue.category.CategoryRepository
import com.dotcom.retail.domain.catalogue.category.attribute.AttributeDataType
import com.dotcom.retail.domain.catalogue.category.attribute.CategoryAttribute
import com.dotcom.retail.domain.catalogue.category.attribute.CategoryAttributeRepository
import com.dotcom.retail.domain.catalogue.category.attribute.FilterType
import com.dotcom.retail.domain.catalogue.product.ProductRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class CategoryAttributeControllerTest : BaseIntegrationTest() {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var categoryAttributeRepository: CategoryAttributeRepository
    @Autowired private lateinit var productRepository: ProductRepository
    @Autowired private lateinit var categoryRepository: CategoryRepository

    private val adminAttributeBase = ApiRoutes.Admin.Category.BASE + ApiRoutes.Admin.Category.ATTRIBUTE

    @BeforeEach
    fun setup() {
        productRepository.deleteAll()
        categoryRepository.deleteAll()
        categoryAttributeRepository.deleteAll()
    }

    @Test
    fun `get should return 200 with category attribute`() {
        val attribute = categoryAttributeRepository.save(
            CategoryAttribute(
                attribute = "test",
                label = "Test",
                dataType = AttributeDataType.TEXT,
                filterType = FilterType.CHECKBOX,
                displayOrder = 1,
                isPublic = true
            )
        )

        mockMvc.perform(get("${ApiRoutes.Category.Attribute.BASE}/${attribute.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(attribute.id))
    }

    @Test
    fun `create should return 201 and persist attribute`() {
        val request = CreateCategoryAttribute(
            attribute = "test",
            label = "Test",
            unit = "kg",
            dataType = AttributeDataType.NUMBER,
            filterType = FilterType.CHECKBOX,
            displayOrder = 1,
            isPublic = true,
            categories = emptyList()
        )

        mockMvc.perform(
            post(adminAttributeBase)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.attribute").value(request.attribute))

        assertEquals(1, categoryAttributeRepository.count())
    }

    @Test
    fun `create should attach attribute to existing category`() {
        val category = categoryRepository.save(Category(name = "Test"))

        val request = CreateCategoryAttribute(
            attribute = "test",
            label = "Test",
            unit = null,
            dataType = AttributeDataType.TEXT,
            filterType = FilterType.DROPDOWN,
            displayOrder = 2,
            isPublic = true,
            categories = listOf(category.id)
        )

        mockMvc.perform(
            post(adminAttributeBase)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        val updatedCategory = categoryRepository.findById(category.id).get()
        assertEquals(1, updatedCategory.attributes.size)
        assertEquals(request.attribute, updatedCategory.attributes[0].attribute)
    }

    @Test
    fun `edit should update properties and category links`() {
        val category1 = categoryRepository.save(Category(name = "Test1"))
        val category2 = categoryRepository.save(Category(name = "Test2"))

        val attribute = categoryAttributeRepository.save(
            CategoryAttribute(
                attribute = "testAttribute1",
                label = "Test1",
                dataType = AttributeDataType.TEXT,
                filterType = FilterType.CHECKBOX,
                displayOrder = 1,
                isPublic = true
            )
        )

        val updateRequest = EditCategoryAttribute(
            id = attribute.id,
            attribute = "testAttribute2",
            label = "Test2",
            unit = null,
            dataType = AttributeDataType.TEXT,
            filterType = FilterType.CHECKBOX,
            displayOrder = 1,
            isPublic = true,
            categories = listOf(category1.id, category2.id)
        )

        mockMvc.perform(
            put("$adminAttributeBase/{id}", attribute.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.label").value(updateRequest.label))

        val updatedAttr = categoryAttributeRepository.findById(attribute.id).get()
        assertEquals(updateRequest.label, updatedAttr.label)
        assertEquals(2, updatedAttr.categories.size)

        val cat1 = categoryRepository.findById(category1.id).get()
        assertTrue(cat1.attributes.any { it.id == attribute.id })
    }

    @Test
    fun `delete should remove attribute`() {
        val attribute = categoryAttributeRepository.save(
            CategoryAttribute(
                attribute = "weight",
                label = "Weight",
                dataType = AttributeDataType.NUMBER,
                filterType = FilterType.SLIDER,
                displayOrder = 5,
                isPublic = true
            )
        )

        mockMvc.perform(delete("$adminAttributeBase/{id}", attribute.id))
            .andExpect(status().isNoContent)

        assertFalse(categoryAttributeRepository.existsById(attribute.id))
    }
}