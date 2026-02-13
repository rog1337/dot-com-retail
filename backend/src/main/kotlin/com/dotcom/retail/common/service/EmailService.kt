package com.dotcom.retail.common.service

import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.config.properties.AppProperties
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    appProperties: AppProperties,
    private val mailSender: JavaMailSender
) {

    private val appUrl = appProperties.url
    private val passwordResetUrl = "$appUrl${ApiRoutes.Auth.BASE}${ApiRoutes.Auth.RESET_PASSWORD_VERIFY}"

    fun sendPasswordReset(to: String, token: String) {
        val message = SimpleMailMessage()
        message.setTo(to)
        message.subject = "Password Reset"

        // TODO
        // This is temporary and will be changed when frontend is added
        message.text = "Reset password with\nPOST $passwordResetUrl\nwith body\n{ token: $token, password: your_new_password }"

        mailSender.send(message)
    }
}