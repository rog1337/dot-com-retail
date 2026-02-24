package com.dotcom.retail.domain.cart

import com.dotcom.retail.common.exception.AppException
import com.dotcom.retail.common.exception.CartError
import com.dotcom.retail.common.exception.ProductError
import com.dotcom.retail.domain.cart.dto.CartUpdateRequest
import com.dotcom.retail.domain.catalogue.product.ProductService
import com.dotcom.retail.domain.user.User
import com.dotcom.retail.domain.user.UserService
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val userService: UserService,
    private val productService: ProductService,
    private val cartItemRepository: CartItemRepository
) {

    companion object {
        const val SESSION_ID_HEADER = "X-Session-Id"
    }

    @Transactional
    fun getActiveCart(userId: UUID?, sessionId: String?): Cart {
        if (userId != null) {
            return findByUserId(userId) ?: createCartForUser(userService.getById(userId))
        }
        if (sessionId != null) {
            return findBySessionId(sessionId) ?: createCartForGuest(sessionId)
        }

        throw AppException(CartError.CART_IDENTIFIER_REQUIRED)
    }

    @Transactional
    fun clearCart(cart: Cart): Cart {
        cart.items.clear()
        return save(cart)
    }

    fun createCart(userId: UUID?, sessionId: String?, productIds: List<Long>?) {

    }

    fun createCartForUser(user: User): Cart {
        return save(Cart(user = user))
    }

    fun createCartForGuest(sessionId: String): Cart {
        return save(Cart(sessionId = sessionId))
    }

    @Transactional
    fun update(userId: UUID?, sessionId: String?, request: List<CartUpdateRequest>?): Cart {
        val cart = getActiveCart(userId, sessionId)
        val productQuantity = request?.associate { it.productId to it.quantity }
        if (productQuantity.isNullOrEmpty()) return clearCart(cart)

        val products = productService.getAllById(productQuantity.keys)

        val cartItems = products.mapNotNull {
            val quantity = productQuantity[it.id]

            if (quantity == null || quantity < 1) return@mapNotNull null
            if (it.stock < quantity) throw AppException(ProductError.PRODUCT_INSUFFICIENT_STOCK.withIdentifier(it.id))

            CartItem(product = it, quantity = quantity)
        }

        clearCart(cart)
        cartItems.forEach { cart.addItem(it) }

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

    fun findByUserId(userId: UUID): Cart? {
        return cartRepository.findByUserId(userId)
    }

    fun findBySessionId(sessionId: String): Cart? {
        return cartRepository.findBySessionId(sessionId)
    }

    fun save(cart: Cart): Cart {
        return cartRepository.save(cart)
    }

    fun saveAllCartItems(cartsItems: List<CartItem>): List<CartItem> {
        return cartItemRepository.saveAll(cartsItems)
    }
}