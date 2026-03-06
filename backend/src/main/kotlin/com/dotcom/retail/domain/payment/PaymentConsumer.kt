package com.dotcom.retail.domain.payment

import com.dotcom.retail.common.service.EmailService
import com.dotcom.retail.config.messaging.RabbitMqConfig
import com.dotcom.retail.domain.order.OrderService
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException
import org.springframework.stereotype.Service

@Service
class PaymentConsumer(
    private val emailService: EmailService,
    private val paymentService: PaymentService,
    private val orderService: OrderService
) {

    val log = LoggerFactory.getLogger(PaymentConsumer::class.java)

    @RabbitListener(queues = [RabbitMqConfig.ORDER_QUEUE])
    @Transactional
    fun consumePaymentEvent(event: PaymentEvent) {
        try {
            //        val transaction = paymentService.getByExternalId(event.paymentId)
//
//        val order = transaction.order

            val order = orderService.getByPaymentIntentId(event.paymentId)

            if (event.status == TransactionStatus.SUCCESS) {
                orderService.handleSuccess(order)
//            emailService.sendOrderConfirmation(order.address.email, order.id, event.paymentId, order.totalAmount)
            } else if (event.status == TransactionStatus.FAILED) {
                orderService.handleFail(order)
//            emailService.sendOrderFailed(order.address.email, order.id)
            }

//        paymentService.saveTransaction(transaction)
        } catch(e: ListenerExecutionFailedException) {
            log.error(e.message, e)
        } catch (e: Exception) {
            log.error(e.message, e)
        }

    }

}