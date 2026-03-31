package com.dotcom.retail.config.security

import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.common.constants.ApiRoutes.Auth

object SecurityMatchers {

    val PUBLIC_ENDPOINTS = arrayOf(
        "${Auth.BASE}${Auth.LOGIN}",
        "${Auth.BASE}${Auth.REGISTER}",
        "${Auth.BASE}${Auth.REFRESH}",
        "${Auth.BASE}${Auth.RESET_PASSWORD}",
        "${Auth.BASE}${Auth.RESET_PASSWORD_VERIFY}",

        "${ApiRoutes.Filter.BASE}",

        "${ApiRoutes.Product.BASE}",
        "${ApiRoutes.Product.BASE}/*",

        "${ApiRoutes.Image.SERVE}",

        "${ApiRoutes.Payment.BASE}${ApiRoutes.Payment.STRIPE}",

        "/error"
    )

    val OPTIONAL_AUTH_ENDPOINTS = arrayOf(
        "${ApiRoutes.Product.BASE}/*${ApiRoutes.Product.REVIEW}",
        ApiRoutes.Cart.BASE,
        ApiRoutes.Order.BASE,
        "${ApiRoutes.Cart.BASE}${ApiRoutes.Cart.CHECKOUT}",
        "${ApiRoutes.Order.BASE}${ApiRoutes.Order.SUBMIT}",
    )

    val ADMIN_ENDPOINTS = arrayOf(
        ApiRoutes.Admin.BASE + "/**",
    )
}