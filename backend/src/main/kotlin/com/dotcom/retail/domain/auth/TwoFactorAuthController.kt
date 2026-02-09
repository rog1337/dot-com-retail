package com.dotcom.retail.domain.auth

import com.dotcom.retail.common.annotation.RequiresTwoFactorAuth
import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.domain.auth.dto.TwoFactorCode
import com.dotcom.retail.domain.auth.dto.TwoFactorSetupResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping(ApiRoutes.TwoFactorAuth.BASE)
class TwoFactorAuthController(private val twoFactorAuthService: TwoFactorAuthService) {

    @PostMapping(ApiRoutes.TwoFactorAuth.SETUP)
    fun setup(@AuthenticationPrincipal userId: UUID): ResponseEntity<TwoFactorSetupResponse> {
        val response = twoFactorAuthService.setup(userId)
        return ResponseEntity.ok().body(response)
    }

    @PostMapping(ApiRoutes.TwoFactorAuth.VERIFY)
    fun verify(@AuthenticationPrincipal userId: UUID, @RequestBody twoFactorCode: TwoFactorCode): ResponseEntity<Void> {
        twoFactorAuthService.verify(userId, twoFactorCode.code)
        return ResponseEntity.ok().build()
    }

    @RequiresTwoFactorAuth
    @PostMapping(ApiRoutes.TwoFactorAuth.DISABLE)
    fun disable(@AuthenticationPrincipal userId: UUID): ResponseEntity<Void> {
        twoFactorAuthService.disable(userId)
        return ResponseEntity.ok().build()
    }
}