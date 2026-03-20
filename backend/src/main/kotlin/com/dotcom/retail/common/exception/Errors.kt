package com.dotcom.retail.common.exception

import org.springframework.http.HttpStatus

data class AppError(
    val code: String,
    val status: HttpStatus,
    val message: String,
    val identifier: Any? = null
) {
    fun withIdentifier(id: Any) = copy(identifier = id)
}

object AuthError {
    val NON_LOCAL_ACCOUNT = AppError("NON_LOCAL_ACCOUNT", HttpStatus.FORBIDDEN, "Non local account")
    val INVALID_CREDENTIALS = AppError("INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED, "Invalid credentials")
    val REQUEST_NOT_AUTHENTICATED = AppError("REQUEST_NOT_AUTHENTICATED", HttpStatus.UNAUTHORIZED, "Request not authenticated")
}

object OAuthError {
    val OAUTH_ERROR = AppError("OAUTH_ERROR", HttpStatus.BAD_REQUEST, "OAuth2 error")
    val OAUTH_UNKNOWN_PROVIDER = AppError("OAUTH_UNKNOWN_PROVIDER", HttpStatus.BAD_REQUEST, "Unknown OAuth2 provider")
    val OAUTH_EMAIL_NOT_VERIFIED = AppError("OAUTH_EMAIL_NOT_VERIFIED", HttpStatus.FORBIDDEN, "OAuth2 email is not verified")
}

object JwtError {
    val JWT_REFRESH_MISSING = AppError("JWT_REFRESH_MISSING", HttpStatus.UNAUTHORIZED, "Refresh token is missing")
    val JWT_REFRESH_REVOKED = AppError("JWT_REFRESH_REVOKED", HttpStatus.UNAUTHORIZED, "Refresh token is revoked")
    val JWT_ACCESS_REVOKED = AppError("JWT_ACCESS_REVOKED", HttpStatus.UNAUTHORIZED, "Access token is revoked")
    val JWT_ROLE_MISSING = AppError("JWT_ROLE_MISSING", HttpStatus.UNAUTHORIZED, "Missing role")
}

object PasswordResetError {
    val PASSWORD_RESET_TOKEN_INVALID = AppError("PASSWORD_RESET_TOKEN_INVALID", HttpStatus.UNAUTHORIZED, "Password reset token is invalid")
}

object TwoFactorAuthError {
    val TWO_FACTOR_SECRET_NOT_SET = AppError("TWO_FACTOR_SECRET_NOT_SET", HttpStatus.FORBIDDEN, "2FA secret is not set")
    val INVALID_TWO_FACTOR_CODE = AppError("INVALID_TWO_FACTOR_CODE", HttpStatus.FORBIDDEN, "2FA secret is not set")
    val TWO_FACTOR_REQUIRED = AppError("TWO_FACTOR_REQUIRED", HttpStatus.FORBIDDEN, "2FA is required")
    val TWO_FACTOR_AUTHENTICATION_ALREADY_ENABLED = AppError("TWO_FACTOR_AUTHENTICATION_ALREADY_ENABLED", HttpStatus.BAD_REQUEST, "2FA authentication already enabled")
}

object CaptchaError {
    val CAPTCHA_FAILED = AppError("CAPTCHA_FAILED", HttpStatus.BAD_REQUEST, "Captcha failed")
    val CAPTCHA_TOKEN_ALREADY_USED = AppError("CAPTCHA_TOKEN_ALREADY_USED", HttpStatus.BAD_REQUEST, "Captcha token already used")
}

object UserError {
    val USER_NOT_FOUND = AppError("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "User not found")
    val USER_ALREADY_EXISTS = AppError("USER_ALREADY_EXISTS", HttpStatus.CONFLICT, "User already exists")
}

object ProductError {
    val PRODUCT_ERROR = AppError("CART_ERROR", HttpStatus.BAD_REQUEST, "Product error")
    val PRODUCT_NOT_FOUND = AppError("CART_ERROR", HttpStatus.BAD_REQUEST, "Product not found")
    val PRODUCT_IDS_NOT_PROVIDED = AppError("PRODUCT_IDS_NOT_PROVIDED", HttpStatus.BAD_REQUEST, "Product ID's were not provided")
    val INVALID_PRODUCT_ATTRIBUTE = AppError("INVALID_PRODUCT_ATTRIBUTE", HttpStatus.BAD_REQUEST, "Invalid product attribute")
    val PRODUCT_INSUFFICIENT_STOCK = AppError("PRODUCT_INSUFFICIENT_STOCK", HttpStatus.CONFLICT, "Not enough stock for product")
}

object CategoryError {
    val CATEGORY_NOT_FOUND = AppError("CATEGORY_NOT_FOUND", HttpStatus.NOT_FOUND, "Category not found")
    val CATEGORY_ALREADY_EXISTS = AppError("CATEGORY_ALREADY_EXISTS", HttpStatus.CONFLICT, "Category already exists")
}

object CategoryAttributeError {
    val CATEGORY_ATTRIBUTE_NOT_FOUND = AppError("CATEGORY_ATTRIBUTE_NOT_FOUND", HttpStatus.NOT_FOUND, "Category attribute not found")
    val CATEGORY_ATTRIBUTE_ALREADY_EXISTS = AppError("CATEGORY_ATTRIBUTE_ALREADY_EXISTS", HttpStatus.CONFLICT, "Category attribute already exists")
}

object BrandError {
    val BRAND_NOT_FOUND = AppError("BRAND_NOT_FOUND", HttpStatus.NOT_FOUND, "Brand not found")
}

object ImageError {
    val IMAGE_NOT_FOUND = AppError("IMAGE_NOT_FOUND", HttpStatus.NOT_FOUND, "Image not found")
    val IMAGE_EMPTY = AppError("IMAGE_EMPTY", HttpStatus.BAD_REQUEST, "Image is empty")
    val NOT_AN_IMAGE = AppError("NOT_AN_IMAGE", HttpStatus.BAD_REQUEST, "File is not an image")
    val IMAGE_NOT_PROVIDED = AppError("IMAGE_NOT_PROVIDED", HttpStatus.BAD_REQUEST, "Image was not provided")
}

object ImageMetadataError {
    val IMAGE_METADATA_NOT_PROVIDED = AppError("IMAGE_METADATA_NOT_PROVIDED", HttpStatus.BAD_REQUEST, "Image metadata was not provided")
    val IMAGE_METADATA_DUPLICATE_SORT_ORDER = AppError("IMAGE_METADATA_DUPLICATE_SORT_ORDER", HttpStatus.BAD_REQUEST, "Image metadata has duplicate sort order")
}

object CartError {
    val CART_ERROR = AppError("CART_ERROR", HttpStatus.BAD_REQUEST, "Cart error")
    val CART_NOT_FOUND = AppError("CART_NOT_FOUND", HttpStatus.NOT_FOUND, "Cart not found")
    val CART_EMPTY = AppError("CART_EMPTY", HttpStatus.BAD_REQUEST, "Cart is empty. Add products to cart first.")
    val CART_IDENTIFIER_REQUIRED = AppError("CART_IDENTIFIER_REQUIRED", HttpStatus.BAD_REQUEST, "Either userId or sessionId is required")
}

object OrderError {
    val ORDER_ERROR = AppError("ORDER_ERROR", HttpStatus.BAD_REQUEST, "Order error")
    val ORDER_NOT_FOUND = AppError("ORDER_NOT_FOUND", HttpStatus.NOT_FOUND, "Order not found")
    val ORDER_EMAIL_REQUIRED = AppError("ORDER_EMAIL_REQUIRED", HttpStatus.BAD_REQUEST, "Email is required")
    val ORDER_MISSING_CHARGE_ID = AppError("ORDER_MISSING_CHARGE_ID", HttpStatus.BAD_REQUEST, "Order missing charge id")
    val ORDER_ACCESS_DENIED = AppError("ORDER_ACCESS_DENIED", HttpStatus.FORBIDDEN, "Order access denied")
}

object TransactionError {
    val TRANSACTION_NOT_FOUND = AppError("TRANSACTION_NOT_FOUND", HttpStatus.NOT_FOUND, "Transaction not found")
    val STRIPE_SIGNATURE_VERIFICATION_FAILED = AppError("STRIPE_SIGNATURE_VERIFICATION_FAILED", HttpStatus.BAD_REQUEST, "Stripe signature verification failed")
    val PAYMENT_STATUS_CANCELLED = AppError("PAYMENT_STATUS_CANCELLED", HttpStatus.CONFLICT, "Payment status is cancelled")
    val REFUND_ORDER_INVALID_STATE = AppError("REFUND_ORDER_INVALID_STATE", HttpStatus.BAD_REQUEST, "Order state not suitable for refund")
    val REFUND_IDENTIFIER_REQUIRED = AppError("REFUND_IDENTIFIER_REQUIRED", HttpStatus.BAD_REQUEST, "Either userId or sessionId is required")
}

object OptimisticLockingError {
    val CONCURRENT_UPDATE_CONFLICT = AppError("CONCURRENT_UPDATE_CONFLICT", HttpStatus.CONFLICT, "Conflict updating an entity concurrently")
}

object ReviewError {
    val REVIEW_ALREADY_EXISTS = AppError("REVIEW_ALREADY_EXISTS", HttpStatus.BAD_REQUEST, "Review already exists")
    val REVIEW_NOT_FOUND = AppError("REVIEW_NOT_FOUND", HttpStatus.NOT_FOUND, "Review not found")
    val REVIEW_INSUFFICIENT_PRIVILEGES = AppError("REVIEW_INSUFFICIENT_PRIVILEGES", HttpStatus.UNAUTHORIZED, "Insufficient privileges")
    val REVIEW_VOTE_NOT_FOUND = AppError("REVIEW_VOTE_NOT_FOUND", HttpStatus.NOT_FOUND, "Review vote not found")
}