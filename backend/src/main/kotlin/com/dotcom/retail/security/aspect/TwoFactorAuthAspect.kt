package com.dotcom.retail.security.aspect

import com.dotcom.retail.common.annotation.RequiresTwoFactorAuth
import com.dotcom.retail.common.exception.AuthException
import com.dotcom.retail.common.exception.TwoFactorAuthException
import com.dotcom.retail.domain.auth.TwoFactorAuthService
import com.dotcom.retail.domain.user.UserService
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.UUID

@Aspect
@Component
class TwoFactorAuthAspect(
    private val userService: UserService,
    private val twoFactorAuthService: TwoFactorAuthService
) {

    @Around("@annotation(requiresTwoFactorAuth)")
    fun verifyTwoFactorAuth(
        joinPoint: ProceedingJoinPoint,
        requiresTwoFactorAuth: RequiresTwoFactorAuth,
    ): Any? {

        val userId = SecurityContextHolder.getContext()
            .authentication
            ?.principal as? UUID
            ?: throw AuthException.notAuthenticated()

        val user = userService.getById(userId)

        if (user.twoFactorEnabled) {
            val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
            val twoFactorCode = request.getHeader(TwoFactorAuthService.TWO_FACTOR_HEADER)
            val secret = user.twoFactorSecret

            if (secret.isNullOrBlank()) throw TwoFactorAuthException.secretNotSet()
            if (twoFactorCode.isNullOrBlank()) throw TwoFactorAuthException(requiresTwoFactorAuth.message)
            if (!twoFactorAuthService.verifyCode(secret, twoFactorCode)) throw TwoFactorAuthException.invalidCode()
        }

        return joinPoint.proceed()
    }

}