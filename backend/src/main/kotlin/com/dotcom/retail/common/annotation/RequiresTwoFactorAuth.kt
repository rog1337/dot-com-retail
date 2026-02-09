package com.dotcom.retail.common.annotation

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresTwoFactorAuth(
    val message: String = "This action requires two-factor authentication"
)