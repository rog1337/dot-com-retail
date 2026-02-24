package com.dotcom.retail.config.messaging

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
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
        const val ORDER_QUEUE = "order_queue"
        const val EXCHANGE = "order_exchange"
        const val ROUTING_KEY = "order.payment.#"
    }

    @Bean
    fun queue(): Queue = Queue(ORDER_QUEUE, true)

    @Bean
    fun exchange(): TopicExchange = TopicExchange(EXCHANGE)

    @Bean
    fun binding(queue: Queue, exchange: TopicExchange): Binding =
        BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY)

    @Bean
    fun messageConverter(): MessageConverter = Jackson2JsonMessageConverter()

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate =
        RabbitTemplate(connectionFactory).apply {
            messageConverter = Jackson2JsonMessageConverter()
        }
}