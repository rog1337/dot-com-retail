package com.dotcom.retail.domain.payment

import com.dotcom.retail.config.messaging.RabbitMqConfig
import com.dotcom.retail.domain.order.OrderStatus
import com.dotcom.retail.domain.payment.dto.PaymentEvent
import com.stripe.model.Event
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import java.io.Serializable

@Service
class PaymentProducer(private val rabbitTemplate: RabbitTemplate) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun sendPaymentEvent(event: PaymentEvent) {
        val routingKey = RabbitMqConfig.ROUTING_KEY + event.status.name.lowercase()
        log.info("Publishing payment event: routingKey=$routingKey intentId=${event.intentId}")
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, routingKey, event)
    }
}