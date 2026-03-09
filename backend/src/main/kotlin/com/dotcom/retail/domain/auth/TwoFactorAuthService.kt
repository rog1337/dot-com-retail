package com.dotcom.retail.domain.auth

import com.dotcom.retail.common.exception.AppException
import com.dotcom.retail.common.exception.TwoFactorAuthError
import com.dotcom.retail.domain.auth.dto.TwoFactorSetupResponse
import com.dotcom.retail.domain.user.UserService
import dev.samstevens.totp.code.DefaultCodeGenerator
import dev.samstevens.totp.code.DefaultCodeVerifier
import dev.samstevens.totp.code.HashingAlgorithm
import dev.samstevens.totp.qr.QrData
import dev.samstevens.totp.qr.ZxingPngQrGenerator
import dev.samstevens.totp.secret.DefaultSecretGenerator
import dev.samstevens.totp.time.SystemTimeProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Base64
import java.util.UUID

@Service
class TwoFactorAuthService(private val userService: UserService) {

    private val secretGenerator = DefaultSecretGenerator()
    private val qrGenerator = ZxingPngQrGenerator()
    private val timeProvider = SystemTimeProvider()
    private val codeVerifier = DefaultCodeVerifier(DefaultCodeGenerator(), timeProvider)
    @Value("\${spring.application.name}") private lateinit var appName: String

    companion object {
        private const val QR_IMAGE_PREFIX = "data:image/png;base64,"
        const val TWO_FACTOR_HEADER = "X-2FA-Code"
    }


    fun setup(userId: UUID): TwoFactorSetupResponse {
        val user = userService.getById(userId)
        if (user.twoFactorEnabled) {
            throw AppException(TwoFactorAuthError.TWO_FACTOR_AUTHENTICATION_ALREADY_ENABLED)
        }

        val secret = generateSecret()
        user.twoFactorSecret = secret
        userService.save(user)

        val qrCode = generateQrCode(user.email, secret)
        val qrCodeImage = generateQrCodeImage(qrCode)
        val qrCodeBase64 = Base64.getEncoder().encodeToString(qrCodeImage)

        return TwoFactorSetupResponse(secret, QR_IMAGE_PREFIX + qrCodeBase64)
    }

    fun verify(userId: UUID, code: String) {
        val user = userService.getById(userId)
        val secret = user.twoFactorSecret ?: throw AppException(TwoFactorAuthError.TWO_FACTOR_SECRET_NOT_SET)
        if (!verifyCode(secret, code)) throw AppException(TwoFactorAuthError.INVALID_TWO_FACTOR_CODE)

        user.twoFactorEnabled = true
        userService.save(user)
    }

    fun disable(userId: UUID) {
        val user = userService.getById(userId)

        user.twoFactorEnabled = false
        userService.save(user)
    }


    private fun generateSecret(): String {
        return secretGenerator.generate()
    }

    private fun generateQrCode(email: String, secret: String): QrData {
        return QrData.Builder()
            .label(email)
            .secret(secret)
            .issuer(appName)
            .algorithm(HashingAlgorithm.SHA1)
            .digits(6)
            .period(30)
            .build()
    }

    private fun generateQrCodeImage(url: QrData): ByteArray {
        return qrGenerator.generate(url)
    }

    fun verifyCode(secret: String, code: String): Boolean {
        return codeVerifier.isValidCode(secret, code)
    }

    fun getStatus(userId: UUID): Boolean {
        val user = userService.getById(userId)
        return user.twoFactorEnabled
    }
}