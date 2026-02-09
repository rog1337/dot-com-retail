package com.dotcom.retail.domain.auth.dto

data class TwoFactorCode(
    val code: String
)

data class TwoFactorSetupResponse(
    val secret: String,
    val qrCode: String
)