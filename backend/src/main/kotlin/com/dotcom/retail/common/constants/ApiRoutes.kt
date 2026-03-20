package com.dotcom.retail.common.constants

object ApiRoutes {
    const val BASE_API = "/api"
    const val V1 = "$BASE_API/v1"

    object User {
        const val BASE = "$V1/user"
    }

    object Account {
        const val BASE = "$V1/account"
        const val DETAILS = "/details"
        const val ORDERS = "/orders"
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
        const val SEARCH = "/search"
        const val REVIEW = "/review"
    }

    object Brand {
        const val BASE = "$V1/brand"
        const val IMAGE = "/image"
    }

    object Category {
        const val BASE = "$V1/category"

        object Attribute {
            const val BASE = "${Category.BASE}/attribute"
        }
    }

    object Image {
        const val SERVE = "/images/**"
        const val BASE = "$V1/image"
    }

    object Filter {
        const val BASE = "$V1/filter"
        const val FILTER = "/filter"
    }

    object Cart {
        const val BASE = "$V1/cart"
        const val CHECKOUT = "/checkout"
    }

    object Order {
        const val BASE = "$V1/order"
        const val SUMMARY = "/summary"
        const val SUBMIT = "/submit"
    }

    object Payment {
        const val BASE = "$V1/payment"
        const val STRIPE = "/webhook/stripe"
        const val REFUND = "/refund"
    }

    object Review {
        const val BASE = "$V1/review"
        const val VOTE = "/vote"
    }

    object Admin {
        const val BASE = "$V1/admin"

        object Product {
            const val BASE = "${Admin.BASE}/product"
        }

        object Category {
            const val BASE = "${Admin.BASE}/category"

            const val ATTRIBUTE = "/attribute"
        }
    }
}