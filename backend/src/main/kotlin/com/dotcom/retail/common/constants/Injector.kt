package com.dotcom.retail.common.constants

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class Injector(
    @Value("\${jwt.access.exp.ms}") private val jwtAccessExp: Long,
    @Value("\${jwt.refresh.exp.ms}") private val jwtRefreshExp: Long
) {

    @PostConstruct
    fun inject() {
        SecurityConstants.ACCESS_TOKEN_EXPIRATION_MS = jwtAccessExp
        SecurityConstants.REFRESH_TOKEN_EXPIRATION_MS = jwtRefreshExp
    }
}