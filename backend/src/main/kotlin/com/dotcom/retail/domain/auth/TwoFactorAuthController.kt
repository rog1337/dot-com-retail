package com.dotcom.retail.domain.auth

import com.dotcom.retail.common.annotation.RequiresTwoFactorAuth
import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.domain.auth.dto.TwoFactorCode
import com.dotcom.retail.domain.auth.dto.TwoFactorSetupResponse
import com.dotcom.retail.domain.auth.dto.TwoFactorStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping(ApiRoutes.TwoFactorAuth.BASE)
class TwoFactorAuthController(private val twoFactorAuthService: TwoFactorAuthService) {

    @GetMapping
    fun status(@AuthenticationPrincipal userId: UUID): ResponseEntity<TwoFactorStatus> {
        return ok().body(TwoFactorStatus(twoFactorAuthService.getStatus(userId)))
    }

    @PostMapping(ApiRoutes.TwoFactorAuth.SETUP)
    fun setup(@AuthenticationPrincipal userId: UUID): ResponseEntity<TwoFactorSetupResponse> {
        val response = twoFactorAuthService.setup(userId)
        return ok().body(response)
    }

    @PostMapping(ApiRoutes.TwoFactorAuth.VERIFY)
    fun verify(@AuthenticationPrincipal userId: UUID, @RequestBody twoFactorCode: TwoFactorCode): ResponseEntity<Void> {
        twoFactorAuthService.verify(userId, twoFactorCode.code)
        return ok().build()
    }

    @RequiresTwoFactorAuth
    @PostMapping(ApiRoutes.TwoFactorAuth.DISABLE)
    fun disable(@AuthenticationPrincipal userId: UUID): ResponseEntity<Void> {
        twoFactorAuthService.disable(userId)
        return ok().build()
    }
}