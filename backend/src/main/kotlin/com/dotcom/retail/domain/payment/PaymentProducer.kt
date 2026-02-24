package com.dotcom.retail.domain.payment

import com.dotcom.retail.config.messaging.RabbitMqConfig
import com.dotcom.retail.domain.order.OrderStatus
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import java.io.Serializable

@Service
class PaymentProducer(private val rabbitTemplate: RabbitTemplate) {

    fun sendPaymentStatus(event: PaymentEvent) {
        val routingKey = RabbitMqConfig.ROUTING_KEY + event.status.name.lowercase()
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, routingKey, event)
    }
}

data class PaymentEvent(
    val paymentId: String,
    val status: TransactionStatus,
) : Serializable