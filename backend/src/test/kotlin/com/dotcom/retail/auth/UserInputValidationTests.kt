package com.dotcom.retail.auth

import com.dotcom.retail.domain.auth.dto.LoginRequest
import com.dotcom.retail.domain.auth.dto.RegisterRequest
import com.dotcom.retail.domain.order.ShippingType
import com.dotcom.retail.domain.order.dto.OrderAddress
import com.dotcom.retail.domain.order.dto.SubmitOrderRequest
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserInputValidationTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    private fun validAddress() = OrderAddress(
        streetLine1 = "Main St 1",
        streetLine2 = null,
        stateOrProvince = null,
        city = "Tallinn",
        postalCode = "10001",
        country = "EE",
    )

    @Nested
    inner class RegisterRequestValidation {

        @Test
        fun `valid register request passes validation`() {
            val req = RegisterRequest(
                email = "user@example.com",
                password = "SecurePass123!",
                displayName = "Alice",
                captchaToken = "token"
            )
            assertTrue(validator.validate(req).isEmpty())
        }

        @Test
        fun `invalid email format fails validation`() {
            val req = RegisterRequest(
                email = "not-an-email",
                password = "SecurePass123!",
                displayName = "Alice",
                captchaToken = "token"
            )
            val violations = validator.validate(req)
            assertTrue(violations.any { it.propertyPath.toString() == "email" })
        }

        @Test
        fun `blank display name fails validation`() {
            val req = RegisterRequest(
                email = "user@example.com",
                password = "SecurePass123!",
                displayName = "",
                captchaToken = "token"
            )
            val violations = validator.validate(req)
            assertTrue(violations.any { it.propertyPath.toString() == "displayName" })
        }

        @Test
        fun `short password fails validation`() {
            val req = RegisterRequest(
                email = "user@example.com",
                password = "abc",
                displayName = "Alice",
                captchaToken = "token"
            )
            val violations = validator.validate(req)
            assertTrue(violations.any { it.propertyPath.toString() == "password" })
        }
    }

    @Nested
    inner class LoginRequestValidation {

        @Test
        fun `valid login request passes validation`() {
            val req = LoginRequest("user@example.com", "password123", null)
            assertTrue(validator.validate(req).isEmpty())
        }

        @Test
        fun `blank email fails validation`() {
            val req = LoginRequest("", "password123", null)
            val violations = validator.validate(req)
            assertTrue(violations.any { it.propertyPath.toString() == "email" })
        }

        @Test
        fun `blank password fails validation`() {
            val req = LoginRequest("user@example.com", "", null)
            val violations = validator.validate(req)
            assertTrue(violations.any { it.propertyPath.toString() == "password" })
        }
    }

    @Nested
    inner class SubmitOrderRequestValidation {

        @Test
        fun `valid submit order request passes validation`() {
            val req = SubmitOrderRequest(
                name = "Jenny Rosen",
                email = "jenny@example.com",
                phone = "+372123456",
                shippingType = ShippingType.STANDARD,
                address = validAddress(),
                notes = null,
            )
            assertTrue(validator.validate(req).isEmpty())
        }

        @Test
        fun `invalid email in order request fails validation`() {
            val req = SubmitOrderRequest(
                name = "Jenny",
                email = "not-valid",
                phone = "+372123456",
                shippingType = ShippingType.STANDARD,
                address = validAddress(),
                notes = null,
            )
            val violations = validator.validate(req)
            assertTrue(violations.any { it.propertyPath.toString() == "email" })
        }

        @Test
        fun `blank name in order request fails validation`() {
            val req = SubmitOrderRequest(
                name = "",
                email = "jenny@example.com",
                phone = "+372123456",
                shippingType = ShippingType.STANDARD,
                address = validAddress(),
                notes = null,
            )
            val violations = validator.validate(req)
            assertTrue(violations.any { it.propertyPath.toString() == "name" })
        }

        @Test
        fun `blank phone in order request fails validation`() {
            val req = SubmitOrderRequest(
                name = "Jenny",
                email = "jenny@example.com",
                phone = "",
                shippingType = ShippingType.STANDARD,
                address = validAddress(),
                notes = null,
            )
            val violations = validator.validate(req)
            assertTrue(violations.any { it.propertyPath.toString() == "phone" })
        }
    }
}
