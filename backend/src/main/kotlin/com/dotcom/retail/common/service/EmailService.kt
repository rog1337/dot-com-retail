package com.dotcom.retail.common.service

import com.dotcom.retail.config.properties.FrontendProperties
import com.dotcom.retail.config.properties.PasswordProperties
import com.dotcom.retail.domain.order.Order
import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val passwordProperties: PasswordProperties,
    private val encryptionService: EncryptionService,
    private val frontendProperties: FrontendProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Async
    fun sendReviewRemoved(email: String, reviewId: Long, reviewBody: String?, cause: String?) {
        val subject = "Your review was removed"

        //language=html
        val body = """
            <h2>Your review with id $reviewId was removed.</h2>
            <p>Review body:
                <p>$$reviewBody</p>    
            </p>
            <p>Cause: 
                <p>$cause</p>
            </p>
        """.trimIndent()

        send(email, subject, body)
    }

    @Async
    fun sendPasswordReset(to: String, token: String) {
        val url = getPasswordResetUrl(token)

        val subject = "Reset your password"
        val body = """
        <h2>Reset your password</h2>
        <p>We received a request to reset the password for your account.</p>
        <p>Click the button below to choose a new password. This link expires in ${passwordProperties.reset.duration.toMinutes()} minutes.</p>
        <p style="margin:24px 0">
          <a href="$url"
             style="background:#111827;color:#fff;padding:12px 24px;border-radius:6px;text-decoration:none;font-size:14px;font-weight:600">
            Reset Password
          </a>
        </p>
        <p style="font-size:12px;color:#9ca3af">
          If the button doesn't work, copy and paste this link into your browser:<br/>
          <a href="$url" style="color:#6b7280;word-break:break-all">$url</a>
        </p>
        <p style="font-size:12px;color:#9ca3af">
          If you didn't request a password reset you can safely ignore this email — 
          your password will not be changed.
        </p>
    """.trimIndent()

        send(to, subject, body)
    }

    @Async
    fun sendOrderConfirmation(order: Order) {
        val to = decryptEmail(order) ?: return
        val name = decryptName(order)

        val subject = "Order #${order.id} Confirmed"
        val body = """
            <h2>Order confirmed</h2>
            <p>Hi${name?.let { " $it" } ?: ""},</p>
            <p>Your payment was successful and your order is now being prepared.</p>
            ${orderSummaryTable(order)}
            <p>You'll receive another email once your order ships.</p>
            <p>Thanks for shopping with us!</p>
        """.trimIndent()

        send(to, subject, body)
    }

    @Async
    fun sendPaymentFailed(order: Order) {
        val to = decryptEmail(order) ?: return
        val name = decryptName(order)

        val subject = "Payment Failed — Order #${order.id}"
        val body = """
            <h2>Payment failed ❌</h2>
            <p>Hi${name?.let { " $it" } ?: ""},</p>
            <p>We couldn't process your payment for <strong>Order #${order.id}</strong> 
               (${formatAmount(order.totalAmount)}).</p>
            <p><strong>What you can do:</strong></p>
            <ul>
              <li>Check your card details and available balance</li>
              <li>Try a different payment method</li>
            </ul>
            <p>
              <a href="${getAccountPageUrl()}"
                 style="background:#4f46e5;color:#fff;padding:12px 24px;border-radius:6px;text-decoration:none">
                View Order
              </a>
            </p>
            <p>If the problem continues, please contact our support team.</p>
        """.trimIndent()

        send(to, subject, body)
    }

    @Async
    fun sendRefundConfirmation(order: Order) {
        val to = decryptEmail(order) ?: return
        val name = decryptName(order)

        val subject = "Refund Processed — Order #${order.id}"
        val body = """
            <h2>Refund on its way 💸</h2>
            <p>Hi${name?.let { " $it" } ?: ""},</p>
            <p>Your refund of <strong>${formatAmount(order.totalAmount)}</strong> 
               for Order #${order.id} has been processed.</p>
            <p>Please allow <strong>5–10 business days</strong> for the funds to 
               appear on your statement, depending on your bank.</p>
            <p>We hope to see you again!</p>
        """.trimIndent()

        send(to, subject, body)
    }

    @Async
    fun sendRefundFailed(order: Order) {
        val to = decryptEmail(order) ?: return
        val name = decryptName(order)

        val body = """
            <h2>Refund issue ⚠️</h2>
            <p>Hi${name?.let { " $it" } ?: ""},</p>
            <p>We ran into a problem processing your refund for Order #${order.id}. 
               Our team has been notified and will resolve this within <strong>1 business day</strong>.</p>
            <p>You do <strong>not</strong> need to take any action — we'll follow up shortly.</p>
            <p>We apologise for the inconvenience.</p>
        """.trimIndent()

        send(to, "Action Required: Refund Issue — Order #${order.id}", body)
    }

    private fun send(to: String, subject: String, htmlBody: String) {
        try {
            val message: MimeMessage = mailSender.createMimeMessage()
            MimeMessageHelper(message, true, "UTF-8").apply {
                setTo(to)
                setSubject(subject)
                setText(htmlBody, true)
            }
            mailSender.send(message)
            log.debug("Email sent to=$to subject='$subject'")
        } catch (ex: Exception) {
            log.error("Failed to send email to=$to subject='$subject'", ex)
        }
    }

    private fun getPasswordResetUrl(token: String): String {
        return passwordProperties.passwordResetUrl + "?token=$token"
    }

    private fun getAccountPageUrl(): String {
        return frontendProperties.accountUrl
    }

    private fun decryptEmail(order: Order): String? {
        return try {
            order.contact?.email?.let { encryptionService.decrypt(it) }
                ?: run { log.warn("No contact email on order ${order.id}"); null }
        } catch (ex: Exception) {
            log.error("Failed to decrypt email for order ${order.id}", ex)
            null
        }
    }

    private fun decryptName(order: Order): String? {
        return try {
            order.contact?.name?.let { encryptionService.decrypt(it) }
        } catch (ex: Exception) {
            log.warn("Failed to decrypt name for order ${order.id}")
            null
        }
    }

    private fun orderSummaryTable(order: Order): String {
        val itemRows = order.items.joinToString("\n") { item ->
            val lineTotal = item.price.multiply(BigDecimal(item.quantity))
            """
            <tr>
              <td style="padding:10px 12px;border-bottom:1px solid #f3f4f6">
                <span style="font-size:14px;color:#111827">${item.productName}</span>
              </td>
              <td style="padding:10px 12px;border-bottom:1px solid #f3f4f6;text-align:center;color:#6b7280;font-size:13px">
                ${formatAmount(item.price)} &times; ${item.quantity}
              </td>
              <td style="padding:10px 12px;border-bottom:1px solid #f3f4f6;text-align:right;font-size:14px;color:#111827;white-space:nowrap">
                ${formatAmount(lineTotal)}
              </td>
            </tr>
            """.trimIndent()
        }

        val shippingLabel = order.shippingType?.name
            ?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Standard"

        return """
        <table style="border-collapse:collapse;width:100%;max-width:560px;margin:20px 0;font-family:sans-serif">
 
          <!-- Header -->
          <tr style="background:#f9fafb">
            <th style="padding:10px 12px;text-align:left;font-size:12px;font-weight:600;color:#6b7280;text-transform:uppercase;letter-spacing:0.05em;border-bottom:2px solid #e5e7eb">
              Product
            </th>
            <th style="padding:10px 12px;text-align:center;font-size:12px;font-weight:600;color:#6b7280;text-transform:uppercase;letter-spacing:0.05em;border-bottom:2px solid #e5e7eb">
              Price &times; Qty
            </th>
            <th style="padding:10px 12px;text-align:right;font-size:12px;font-weight:600;color:#6b7280;text-transform:uppercase;letter-spacing:0.05em;border-bottom:2px solid #e5e7eb">
              Amount
            </th>
          </tr>
 
          <!-- Items -->
          $itemRows
 
          <!-- Subtotal -->
          <tr>
            <td colspan="2" style="padding:8px 12px;text-align:right;font-size:13px;color:#6b7280;border-top:1px solid #e5e7eb">
              Subtotal
            </td>
            <td style="padding:8px 12px;text-align:right;font-size:13px;color:#374151;border-top:1px solid #e5e7eb">
              ${formatAmount(order.totalAmount.subtract(order.shippingCost ?: BigDecimal.ZERO))}
            </td>
          </tr>
 
          <!-- Shipping -->
          <tr>
            <td colspan="2" style="padding:8px 12px;text-align:right;font-size:13px;color:#6b7280">
              Shipping ($shippingLabel)
            </td>
            <td style="padding:8px 12px;text-align:right;font-size:13px;color:#374151">
              ${if (order.shippingCost == null || order.shippingCost == BigDecimal.ZERO) "Free" else formatAmount(order.shippingCost!!)}
            </td>
          </tr>
 
          <!-- Total -->
          <tr style="background:#f9fafb">
            <td colspan="2" style="padding:12px;text-align:right;font-size:15px;font-weight:700;color:#111827;border-top:2px solid #e5e7eb">
              Total
            </td>
            <td style="padding:12px;text-align:right;font-size:15px;font-weight:700;color:#111827;border-top:2px solid #e5e7eb;white-space:nowrap">
              ${formatAmount(order.totalAmount)}
            </td>
          </tr>
 
        </table>
 
        <!-- Shipping address -->
        ${order.contact?.address?.let { addr ->
            try {
                val street = encryptionService.decrypt(addr.streetLine1)
                val line2 = addr.streetLine2?.let { encryptionService.decrypt(it) }
                val city = encryptionService.decrypt(addr.city)
                val postal = addr.postalCode?.let {encryptionService.decrypt(it) }
                val country = encryptionService.decrypt(addr.country)
                """
                <p style="font-size:13px;color:#6b7280;margin:4px 0 2px">
                  <strong style="color:#374151">Shipping to:</strong>
                  $street${line2 ?: ""}, $city ${postal ?: ""}, $country
                </p>
                """.trimIndent()
            } catch (ex: Exception) {
                log.warn("Could not decrypt address for order ${order.id} in email")
                ""
            }
        } ?: ""}
        """.trimIndent()
    }

    private fun formatAmount(amount: BigDecimal) = "€${"%.2f".format(amount)}"
}