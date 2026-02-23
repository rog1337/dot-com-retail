package com.dotcom.retail.security.aspect

import com.dotcom.retail.common.annotation.RequiresTwoFactorAuth
import com.dotcom.retail.common.exception.AppException
import com.dotcom.retail.common.exception.AuthError
import com.dotcom.retail.common.exception.TwoFactorAuthError
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
            ?: throw AppException(AuthError.REQUEST_NOT_AUTHENTICATED)

        val user = userService.getById(userId)

        if (user.twoFactorEnabled) {
            val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
            val twoFactorCode = request.getHeader(TwoFactorAuthService.TWO_FACTOR_HEADER)
            val secret = user.twoFactorSecret

            if (secret.isNullOrBlank()) throw AppException(TwoFactorAuthError.TWO_FACTOR_SECRET_NOT_SET)
            if (twoFactorCode.isNullOrBlank()) throw AppException(TwoFactorAuthError.TWO_FACTOR_REQUIRED)
            if (!twoFactorAuthService.verifyCode(secret, twoFactorCode)) throw AppException(TwoFactorAuthError.INVALID_TWO_FACTOR_CODE)
        }

        return joinPoint.proceed()
    }

}