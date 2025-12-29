package com.dotcom.retail.common.constants

object ApiRoutes {
    const val BASE_API = "/api"
    const val V1 = "$BASE_API/v1"

    object Auth {
        const val BASE = "$V1/auth"
        const val REGISTER = "/register"
        const val LOGIN = "/login"
        const val REFRESH = "/refresh"
        const val REFRESH_FULL = "$BASE$REFRESH"
    }

    object OAuth {
        const val BASE = "$V1/oauth2"
    }

    object Product {
        const val BASE = "$V1/product"
        const val IMAGE = "/image"
        const val SLUG = "/slug"
    }

    object Brand {
        const val BASE = "$V1/brand"
        const val IMAGE = "/image"
    }

    object Image {
        const val BASE = "$V1/image"
        const val PRODUCT = "/product"
        const val BRAND = "/brand"
    }
}