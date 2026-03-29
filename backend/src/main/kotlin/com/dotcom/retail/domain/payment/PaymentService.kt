package com.dotcom.retail.domain.payment

import com.dotcom.retail.common.exception.AppException
import com.dotcom.retail.common.exception.OrderError
import com.dotcom.retail.common.exception.TransactionError
import com.dotcom.retail.common.service.EmailService
import com.dotcom.retail.config.properties.StripeProperties
import com.dotcom.retail.domain.order.OrderRepository
import com.dotcom.retail.domain.order.OrderStatus
import com.dotcom.retail.domain.payment.dto.PaymentEvent
import com.dotcom.retail.domain.payment.dto.RefundRequest
import com.dotcom.retail.domain.payment.dto.TransactionStatus
import com.stripe.model.Charge
import com.stripe.model.Event
import com.stripe.model.PaymentIntent
import com.stripe.model.Refund
import com.stripe.net.Webhook
import com.stripe.param.PaymentIntentCreateParams
import com.stripe.param.PaymentIntentUpdateParams
import com.stripe.param.RefundCreateParams
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class PaymentService(
    private val stripeProperties: StripeProperties,
    private val paymentProducer: PaymentProducer,
    private val orderRepository: OrderRepository,
    private val emailService: EmailService,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val STRIPE_SIGNATURE_HEADER = "Stripe-Signature"
        const val CURRENCY = "EUR"

        object RefundReason {
            const val DUPLICATE = "duplicate"
            const val FRAUDULENT = "fraudulent"
        }
    }

    fun createPaymentIntent(amount: BigDecimal): PaymentIntent {
        val params = PaymentIntentCreateParams.builder()
            .setAmount(toStripeMoney(amount))
            .setCurrency(CURRENCY)
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
            )
            .build()

        val paymentIntent = PaymentIntent.create(params)

        return paymentIntent
    }

    fun updatePaymentIntent(paymentIntent: PaymentIntent, amount: BigDecimal): PaymentIntent {
        val newAmount = toStripeMoney(amount)
        val params = PaymentIntentUpdateParams.builder()
            .setAmount(newAmount)
            .putMetadata("updated_at", System.currentTimeMillis().toString())
            .build()

        return paymentIntent.update(params)
    }

    fun updatePaymentIntent(paymentIntentId: String, amount: BigDecimal): PaymentIntent {
        return updatePaymentIntent(PaymentIntent.retrieve(paymentIntentId), amount)
    }

    fun cancelPaymentIntent(intentId: String) {
        val intent = PaymentIntent.retrieve(intentId)
        if (intent.status in listOf("requires_payment_method", "requires_confirmation", "requires_action")) {
            intent.cancel()
            log.info("Cancelled PaymentIntent intentId=$intentId")
        } else {
            log.warn("PaymentIntent intentId=$intentId is in state=${intent.status} — cannot cancel")
        }
    }

    fun handleStripeEvent(payload: String, sigHeader: String) {
        val event: Event = Webhook.constructEvent(payload, sigHeader, stripeProperties.webhookSecret)

        when (event.type) {
            "payment_intent.succeeded" -> {
                val intent = event.dataObjectDeserializer.`object`.get() as PaymentIntent
                paymentProducer.sendPaymentEvent(
                    PaymentEvent(
                        intentId = intent.id,
                        status = TransactionStatus.SUCCESS,
                        chargeId = intent.latestCharge,
                        amount = fromStripeMoney(intent.amount),
                        currency = intent.currency,
                    )
                )
            }

            "payment_intent.payment_failed" -> {
                val intent = event.dataObjectDeserializer.`object`.get() as PaymentIntent
                log.info("Payment attempt failed for intent=${intent.id} reason=${intent.lastPaymentError?.message} — intent still open")
            }

            "payment_intent.canceled" -> {
                // todo clear cart
                val intent = event.dataObjectDeserializer.`object`.get() as PaymentIntent
                paymentProducer.sendPaymentEvent(
                    PaymentEvent(
                        intentId = intent.id,
                        status = TransactionStatus.CANCELLED,
                        failureReason = intent.cancellationReason,
                        amount = fromStripeMoney(intent.amount),
                        currency = intent.currency,
                    )
                )
            }

            "charge.refunded" -> {
                val charge = event.dataObjectDeserializer.`object`.get() as Charge
                val intentId = charge.paymentIntent
                if (intentId == null) {
                    log.warn("charge.refunded has no paymentIntent — skipping (chargeId=${charge.id})")
                    return
                }
                val refundId = charge.refunds?.data?.firstOrNull()?.id
                paymentProducer.sendPaymentEvent(
                    PaymentEvent(
                        intentId = intentId,
                        status = TransactionStatus.REFUNDED,
                        chargeId = charge.id,
                        refundId = refundId,
                        amount = fromStripeMoney(charge.amountRefunded),
                        currency = charge.currency,
                    )
                )
            }

            else -> log.debug("Unhandled Stripe event type=${event.type} — ignoring")
        }
    }

    fun initiateRefund(orderId: UUID, request: RefundRequest?) {
        val order = orderRepository.findById(orderId)
            .orElseThrow { AppException(OrderError.ORDER_NOT_FOUND.withIdentifier(orderId)) }

        if (order.status != OrderStatus.PAID) {
            throw AppException(TransactionError.REFUND_ORDER_ILLEGAL_STATE.withIdentifier(order.status))
        }

        val chargeId = order.chargeId
            ?: throw AppException(OrderError.ORDER_MISSING_CHARGE_ID)

        refundCharge(chargeId, order.totalAmount, request?.reason)

        order.status = OrderStatus.REFUND_PENDING
        orderRepository.save(order)

        emailService.sendRefundConfirmation(order)
        log.info("Refund initiated for order=$orderId intentId=${order.intentId}")
    }

    fun refundCharge(chargeId: String, amount: BigDecimal, reason: String? = null) {
        val params = RefundCreateParams.builder()
            .setCharge(chargeId)
            .setAmount(toStripeMoney(amount))
            .apply {
                reason?.let {
                    setReason(
                        when (it.lowercase()) {
                            RefundReason.DUPLICATE  -> RefundCreateParams.Reason.DUPLICATE
                            RefundReason.FRAUDULENT -> RefundCreateParams.Reason.FRAUDULENT
                            else                    -> RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER
                        }
                    )
                }
            }
            .build()
        Refund.create(params)
        log.info("Refunded charge=$chargeId amount=$amount")
    }

    fun retrieveIntent(paymentIntentId: String): PaymentIntent {
        return PaymentIntent.retrieve(paymentIntentId)
    }

    private fun toStripeMoney(value: BigDecimal): Long =
        value.multiply(BigDecimal(100)).toLong()

    private fun fromStripeMoney(cents: Long): BigDecimal =
        BigDecimal(cents).divide(BigDecimal(100))
}