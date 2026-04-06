package com.dotcom.retail.product

import com.dotcom.retail.domain.admin.product.dto.CreateProduct
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ProductModelTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    private fun validDto(
        name: String = "Widget Pro",
        sku: String = "WGT-001",
        description: String? = null,
        price: BigDecimal = BigDecimal("19.99"),
        salePrice: BigDecimal = BigDecimal("14.99"),
        stock: Int = 10,
        brandId: Long? = null,
        categoryId: Long? = null,
        isActive: Boolean = false,
    ) = CreateProduct(
        name = name,
        sku = sku,
        description = description,
        price = price,
        salePrice = salePrice,
        stock = stock,
        brandId = brandId,
        categoryId = categoryId,
        isActive = isActive,
    )

    @Test
    fun `valid CreateProduct passes all constraints`() {
        val violations = validator.validate(validDto(isActive = true))
        assertTrue(violations.isEmpty(), "Expected no violations but got: $violations")
    }

    @Test
    fun `blank name fails NotBlank constraint`() {
        val violations = validator.validate(validDto(name = ""))
        assertTrue(violations.any { it.propertyPath.toString() == "name" })
    }

    @Test
    fun `blank sku fails NotBlank constraint`() {
        val violations = validator.validate(validDto(sku = "   "))
        assertTrue(violations.any { it.propertyPath.toString() == "sku" })
    }

    @Test
    fun `negative price fails Min constraint`() {
        val violations = validator.validate(validDto(price = BigDecimal("-1.00")))
        assertTrue(violations.any { it.propertyPath.toString() == "price" })
    }

    @Test
    fun `negative salePrice fails Min constraint`() {
        val violations = validator.validate(validDto(salePrice = BigDecimal("-5.00")))
        assertTrue(violations.any { it.propertyPath.toString() == "salePrice" })
    }

    @Test
    fun `negative stock fails Min constraint`() {
        val violations = validator.validate(validDto(stock = -3))
        assertTrue(violations.any { it.propertyPath.toString() == "stock" })
    }

    @Test
    fun `zero price is valid`() {
        val violations = validator.validate(validDto(price = BigDecimal.ZERO, salePrice = BigDecimal.ZERO, stock = 0))
        assertTrue(violations.isEmpty(), "Zero price should be valid")
    }

    @Test
    fun `multiple violations are reported simultaneously`() {
        val dto = validDto(name = "", sku = "", price = BigDecimal("-1"), salePrice = BigDecimal("-1"), stock = -1)
        val violations = validator.validate(dto)
        assertTrue(violations.size >= 5, "Expected at least 5 violations")
    }

    @Test
    fun `null description is valid`() {
        val violations = validator.validate(validDto(description = null))
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `isActive defaults to false`() {
        val dto = validDto()
        assertEquals(false, dto.isActive)
    }
}
