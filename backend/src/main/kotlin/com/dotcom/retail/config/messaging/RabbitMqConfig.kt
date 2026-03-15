package com.dotcom.retail.config.messaging

import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMqConfig {

    companion object {
        const val EXCHANGE       = "payment.exchange"
        const val RETRY_EXCHANGE = "payment.retry.exchange"
        const val DLX            = "payment.dlx"

        const val ORDER_QUEUE       = "payment.order.queue"
        const val ORDER_RETRY_QUEUE = "payment.order.retry.queue"
        const val ORDER_DEAD_QUEUE  = "payment.order.dead.queue"

        const val ROUTING_KEY       = "payment.order."
        const val QUEUE_BINDING_KEY = "payment.order.*"

        const val MAX_RETRY_COUNT = 3
        const val RETRY_TTL_MS    = 30_000L
    }

    @Bean fun paymentExchange() = TopicExchange(EXCHANGE)
    @Bean fun retryExchange()   = TopicExchange(RETRY_EXCHANGE)
    @Bean fun deadExchange()    = TopicExchange(DLX)

    @Bean
    fun orderQueue() = QueueBuilder.durable(ORDER_QUEUE)
        .withArgument("x-dead-letter-exchange", RETRY_EXCHANGE)
        .withArgument("x-dead-letter-routing-key", "${ROUTING_KEY}retry")
        .build()

    @Bean
    fun orderRetryQueue() = QueueBuilder.durable(ORDER_RETRY_QUEUE)
        .withArgument("x-dead-letter-exchange", EXCHANGE)
        .withArgument("x-dead-letter-routing-key", "${ROUTING_KEY}success") // re-route to main
        .withArgument("x-message-ttl", RETRY_TTL_MS)
        .build()

    @Bean
    fun orderDeadQueue() = QueueBuilder.durable(ORDER_DEAD_QUEUE).build()

    @Bean
    fun orderQueueBinding() =
        BindingBuilder.bind(orderQueue()).to(paymentExchange()).with(QUEUE_BINDING_KEY)

    @Bean
    fun orderRetryBinding() =
        BindingBuilder.bind(orderRetryQueue()).to(retryExchange()).with("${ROUTING_KEY}retry")

    @Bean
    fun orderDeadBinding() =
        BindingBuilder.bind(orderDeadQueue()).to(deadExchange()).with("${ROUTING_KEY}dead")

    @Bean
    fun messageConverter(): MessageConverter = Jackson2JsonMessageConverter()

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate =
        RabbitTemplate(connectionFactory).apply {
            messageConverter = Jackson2JsonMessageConverter()
        }
}