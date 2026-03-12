package com.dotcom.retail.domain.cart

import com.dotcom.retail.common.exception.AppException
import com.dotcom.retail.common.exception.CartError
import com.dotcom.retail.common.exception.ProductError
import com.dotcom.retail.domain.cart.dto.CartUpdateRequest
import com.dotcom.retail.domain.catalogue.product.ProductService
import com.dotcom.retail.domain.order.ShippingType
import com.dotcom.retail.domain.payment.PaymentService
import com.dotcom.retail.domain.user.User
import com.dotcom.retail.domain.user.UserService
import com.stripe.exception.InvalidRequestException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val userService: UserService,
    private val productService: ProductService,
    private val paymentService: PaymentService,
) {
    companion object {
        const val SESSION_ID_HEADER = "X-Session-Id"
        val SHIPPING_STANDARD_PRICE = BigDecimal(5)
        val SHIPPING_EXPRESS_PRICE = BigDecimal(15)
    }

    @Transactional
    fun getActiveCart(userId: UUID?, sessionId: String?): Cart {
        if (userId != null) {
            return findByUserId(userId) ?: createCartForUser(userService.getById(userId))
        }
        if (sessionId != null) {
            return findBySessionId(sessionId) ?: createCartForGuest()
        }

        return createCartForGuest()
    }

    @Transactional
    fun clearCart(cart: Cart): Cart {
        cart.items.clear()
        return save(cart)
    }

    fun createCartForUser(user: User): Cart {
        return save(Cart(user = user))
    }

    fun createCartForGuest(): Cart {
        val sessionId = UUID.randomUUID().toString()
        return save(Cart(sessionId = sessionId))
    }

    @Transactional
    fun update(userId: UUID?, sessionId: String?, request: CartUpdateRequest): Cart {
        val cart = getActiveCart(userId, sessionId)
        val productQuantity = request.items?.associate { it.productId to it.quantity }
        if (productQuantity.isNullOrEmpty()) return clearCart(cart)

        val products = productService.getAllById(productQuantity.keys)
        val cartItems = cart.items
        cartItems.removeIf { it.product.id !in productQuantity.keys }

        val existingProductIds = cartItems.map { it.product.id }

        products.forEach { product ->
            val quantity = productQuantity.getValue(product.id)

            if (product.stock < quantity) {
                throw AppException(ProductError.PRODUCT_INSUFFICIENT_STOCK.withIdentifier(product.id))
            }

            if (product.id in existingProductIds) {
                cartItems.find { it.product.id == product.id }?.quantity = quantity
            } else {
                cart.addItem(CartItem(product = product, quantity = quantity, priceSnapshot = product.price))
            }
        }

        request.shippingType?.let {
            cart.shippingType = it
            cart.shippingCost = calculateShippingCost(it)
        }

        cart.intentId?.let {
            try {
                paymentService.updatePaymentIntent(it, cart.getTotalPrice())
            } catch (e: InvalidRequestException) {
                cart.intentId = null
            }
        }

        return save(cart)
    }

    fun getCart(userId: UUID?, sessionId: String?): Cart {
        if (userId != null) {
            return findByUserId(userId) ?: throw AppException(CartError.CART_NOT_FOUND.withIdentifier(userId))
        }
        if (sessionId != null) {
            return findBySessionId(sessionId) ?: throw AppException(CartError.CART_NOT_FOUND.withIdentifier(sessionId))
        }

        throw AppException(CartError.CART_IDENTIFIER_REQUIRED)
    }

    fun getCartWithLock(userId: UUID?, sessionId: String?): Cart {
        if (userId != null) {
            val cart = cartRepository.lockByUserId(userId)
                ?: throw AppException(CartError.CART_NOT_FOUND.withIdentifier(userId))

            return getCartById(cart.id)
        }
        if (sessionId != null) {
            val cart = cartRepository.lockBySessionId(sessionId)
                ?: throw AppException(CartError.CART_NOT_FOUND.withIdentifier(sessionId))
            return getCartById(cart.id)
        }

        throw AppException(CartError.CART_IDENTIFIER_REQUIRED)
    }

    fun checkStock(cart: Cart) {
        cart.items.forEach { item ->
            if (item.product.stock < item.quantity) {
                throw AppException(ProductError.PRODUCT_INSUFFICIENT_STOCK.withIdentifier(item.product.id))
            }
        }
    }

    fun calculateShippingCost(shippingType: ShippingType): BigDecimal {
        return when (shippingType) {
            ShippingType.STANDARD -> SHIPPING_STANDARD_PRICE
            ShippingType.EXPRESS -> SHIPPING_EXPRESS_PRICE
        }
    }

    fun getCartById(cartId: UUID): Cart {
        return cartRepository.findById(cartId)
            .orElseThrow { AppException(CartError.CART_NOT_FOUND.withIdentifier(cartId)) }!!
    }

    fun findByUserId(userId: UUID): Cart? {
        return cartRepository.findByUserId(userId)
    }

    fun findBySessionId(sessionId: String): Cart? {
        return cartRepository.findBySessionId(sessionId)
    }

    fun save(cart: Cart): Cart {
        return cartRepository.save(cart)
    }

    fun delete(cart: Cart) {
        cartRepository.delete(cart)
    }

    fun getByPaymentIntentId(paymentIntentId: String): Cart {
        return cartRepository.findByIntentId(paymentIntentId)
            ?: throw AppException(CartError.CART_NOT_FOUND.withIdentifier(paymentIntentId))
    }
}