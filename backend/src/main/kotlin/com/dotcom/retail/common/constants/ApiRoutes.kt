package com.dotcom.retail.common.constants

object ApiRoutes {
    const val BASE_API = "/api"
    const val V1 = "$BASE_API/v1"

    object Admin {
        const val BASE = "$V1/admin"
        const val ADMIN = "/admin"
    }

    object Auth {
        const val BASE = "$V1/auth"
        const val REGISTER = "/register"
        const val LOGIN = "/login"
        const val REFRESH = "/refresh"
        const val LOGOUT = "/logout"
        const val RESET_PASSWORD = "/reset-password"
        const val RESET_PASSWORD_VERIFY = "/reset-password-verify"
        const val REFRESH_FULL = "$BASE$REFRESH"
    }

    object TwoFactorAuth {
        const val BASE = "$V1/2fa"
        const val SETUP = "/setup"
        const val VERIFY = "/verify"
        const val DISABLE = "/disable"
    }

    object OAuth {
        const val BASE = "$V1/oauth2"
    }

    object Product {
        const val BASE = "$V1/product"
        const val IMAGE = "/image"
        const val SEARCH = "/search"
    }

    object Brand {
        const val BASE = "$V1/brand"
        const val IMAGE = "/image"
    }

    object Category {
        const val BASE = "$V1/category"
    }

    object Image {
        const val BASE = "$V1/image"
        const val PRODUCT = "/product"
        const val BRAND = "/brand"
    }

    object Filter {
        const val BASE = "$V1/filter"
        const val FILTER = "/filter"
    }
}