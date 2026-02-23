package com.dotcom.retail.common.service

import com.dotcom.retail.config.properties.PasswordProperties
import com.dotcom.retail.domain.auth.PasswordResetService
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val passwordProperties: PasswordProperties
) {

    fun sendPasswordReset(to: String, token: String) {
        val message = SimpleMailMessage()
        message.setTo(to)
        message.subject = "Password Reset"

        // TODO
        // This is temporary and will be changed when frontend is added
        message.text = "Reset password with\nPOST ${passwordProperties.passwordResetUrl}\nwith body\n{ token: $token, password: your_new_password }"

        mailSender.send(message)
    }

    fun sendOrderConfirmation(email: String, id: UUID, paymentId: String, totalAmount: BigDecimal) {
        val message = SimpleMailMessage()
        message.setTo(email)
        message.subject = "Order Confirmation"
        message.text = "Your order with id $id was received. Payment id: $paymentId. Total amount: $totalAmount"
        mailSender.send(message)
    }
}