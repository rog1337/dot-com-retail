package com.dotcom.retail.domain.auth

import com.dotcom.retail.common.exception.AppException
import com.dotcom.retail.common.exception.PasswordResetError
import com.dotcom.retail.common.exception.UserError
import com.dotcom.retail.common.service.EmailService
import com.dotcom.retail.config.properties.PasswordProperties
import com.dotcom.retail.domain.auth.dto.PasswordResetRequest
import com.dotcom.retail.domain.auth.dto.PasswordResetVerification
import com.dotcom.retail.domain.user.UserRepository
import com.dotcom.retail.domain.user.UserService
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

@Service
class PasswordResetService(
    private val userService: UserService,
    private val emailService: EmailService,
    private val redisTemplate: StringRedisTemplate,
    private val passwordEncoder: PasswordEncoder,
    passwordProperties: PasswordProperties,
    private val userRepository: UserRepository,
) {
    private val passwordResetDuration: Duration = passwordProperties.reset.duration

    companion object {
        private const val PASSWORD_RESET_PREFIX = "pwd_reset:"
    }

    fun initiatePasswordResetByEmail(email: String) {
        val email = email.trim()
        if (!userRepository.existsByEmail(email)) {
            throw AppException(UserError.USER_NOT_FOUND.withIdentifier(email))
        }
        initiatePasswordReset(email)
    }

    fun initiatePasswordResetByUserId(userId: UUID) {
        val user = userService.getById(userId)
        initiatePasswordReset(user.email)
    }

    private fun initiatePasswordReset(email: String) {
        userService.getByEmail(email)
        val token = createToken(email)
        emailService.sendPasswordReset(email, token)
    }

    fun resetPassword(data: PasswordResetVerification) {
        val email = getEmail(data.token) ?: throw AppException(PasswordResetError.PASSWORD_RESET_TOKEN_INVALID)
        val user = userService.getByEmail(email)
        user.passwordHash = passwordEncoder.encode(data.password)
        userService.save(user)

        redisTemplate.delete(PASSWORD_RESET_PREFIX + data.token)
    }

    fun getEmail(token: String): String? {
        return redisTemplate.opsForValue().get(PASSWORD_RESET_PREFIX + token)
    }

    fun createToken(email: String): String {
        val token = UUID.randomUUID().toString()
        redisTemplate.opsForValue().set(PASSWORD_RESET_PREFIX + token, email, passwordResetDuration)
        return token
    }
}