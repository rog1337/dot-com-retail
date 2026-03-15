package com.dotcom.retail.domain.payment

import com.dotcom.retail.common.service.EmailService
import com.dotcom.retail.config.messaging.RabbitMqConfig
import com.dotcom.retail.domain.order.Order
import com.dotcom.retail.domain.order.OrderService
import com.dotcom.retail.domain.payment.dto.PaymentEvent
import com.dotcom.retail.domain.payment.dto.TransactionStatus
import com.rabbitmq.client.Channel
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentConsumer(
    private val emailService: EmailService,
    private val paymentService: PaymentService,
    private val orderService: OrderService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = [RabbitMqConfig.ORDER_QUEUE])
    @Transactional
    fun consumePaymentEvent(
        event: PaymentEvent,
        channel: Channel,
        @Header(AmqpHeaders.DELIVERY_TAG) deliveryTag: Long,
        @Header("x-death", required = false) xDeath: List<Map<String, Any>>?,
        ) {
        val retryCount = xDeath?.firstOrNull()?.get("count")?.toString()?.toLongOrNull() ?: 0L
        log.info("Consuming payment event: intentId=${event.intentId} status=${event.status} retry=${retryCount}")
        val order = orderService.findByPaymentIntentId(event.intentId)
        if (order == null) {
            log.error("Order not found for intentId=${event.intentId} — discarding message")
            channel.basicAck(deliveryTag, false)
            return
        }

        try {
            when (event.status) {

                TransactionStatus.SUCCESS -> {
                    if (!order.isComplete()) {
                        log.error("Order ${order.id} has no items or contact — refunding charge=${event.chargeId}")
                        paymentService.refundCharge(
                            chargeId = event.chargeId!!,
                            amount = event.amount,
                            reason = "fraudulent"
                        )
                        orderService.handleCancel(order, "Payment received but order was incomplete")
                        return
                    }
                    val updated = orderService.handleSuccess(order, event.chargeId)
//                    emailService.sendOrderConfirmation(updated)
                    log.info("Order ${order.id} PAID — chargeId=${event.chargeId}")
                }

                TransactionStatus.CANCELLED -> {
                    val updated = orderService.handleCancel(order, event.failureReason)
//                    emailService.sendPaymentFailed(updated)
                    log.warn("Order ${order.id} CANCELLED — reason=${event.failureReason}")
                }

                TransactionStatus.REFUNDED -> {
                    val updated = orderService.handleRefund(order, event.refundId)
//                    emailService.sendRefundConfirmation(updated)
                    log.info("Order ${order.id} REFUNDED — refundId=${event.refundId}")
                }

                TransactionStatus.REFUND_FAILED -> {
                    val updated = orderService.handleRefundFail(order, event.failureReason)
//                    emailService.sendRefundFailed(updated)
                    log.error("Order ${order.id} REFUND_FAILED — needs manual review")
                }

                else -> {
                    log.error("Event handler not implemented: {} - ignoring", event.status)
                }
            }

            channel.basicAck(deliveryTag, false)

        } catch (ex: Exception) {
            log.error("Unexpected error processing intentId=${event.intentId}", ex)
            if (retryCount >= RabbitMqConfig.MAX_RETRY_COUNT) {
                log.error("Exhausted retries for intentId=${event.intentId} — discarding to dead queue")
                channel.basicAck(deliveryTag, false)
            } else {
                channel.basicNack(deliveryTag, false, false)
            }
        }
    }

}