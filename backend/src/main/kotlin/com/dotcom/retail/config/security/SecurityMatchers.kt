package com.dotcom.retail.config.security

import com.dotcom.retail.common.constants.ApiRoutes.Auth

object SecurityMatchers {

    val PUBLIC_ENDPOINTS = arrayOf(
        "${Auth.BASE}${Auth.LOGIN}",
        "${Auth.BASE}${Auth.REGISTER}",
        "${Auth.BASE}${Auth.REFRESH}",

        "/error"
    )
}