package com.dotcom.retail.domain.payment

import com.dotcom.retail.common.service.EmailService
import com.dotcom.retail.config.messaging.RabbitMqConfig
import com.dotcom.retail.domain.order.OrderService
import jakarta.transaction.Transactional
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
class PaymentConsumer(
    private val emailService: EmailService,
    private val paymentService: PaymentService,
    private val orderService: OrderService
) {

    @RabbitListener(queues = [RabbitMqConfig.ORDER_QUEUE])
    @Transactional
    fun consumePaymentEvent(event: PaymentEvent) {
        val transaction = paymentService.getByExternalId(event.paymentId)

        val order = transaction.order

        if (event.status == TransactionStatus.SUCCESS) {
            transaction.status = TransactionStatus.SUCCESS
            orderService.handleSuccess(order)
            emailService.sendOrderConfirmation(order.email, order.id, event.paymentId, order.totalAmount)
        } else if (event.status == TransactionStatus.FAILED) {
            transaction.status = TransactionStatus.FAILED
            orderService.handleFail(order)
            emailService.sendOrderFailed(order.email, order.id)
        }

        paymentService.saveTransaction(transaction)
    }

}