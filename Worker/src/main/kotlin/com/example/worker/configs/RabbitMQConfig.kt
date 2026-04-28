package com.example.worker.configs

import com.example.worker.services.models.SubTaskModel
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.DefaultJacksonJavaTypeMapper
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.json.JsonMapper

@Configuration
class RabbitMQConfig {

    companion object {
        const val EXCHANGE_SUBTASKS_REQUEST = "subtasks.request.exchange"
        const val EXCHANGE_DLQ = "dlq.exchange"
        const val EXCHANGE_SUBTASKS_RESULT = "subtasks.result.exchange"
        const val EXCHANGE_DLQ_NOTIFICATION = "dlq.notification.exchange"

        const val QUEUE_SUBTASKS_REQUEST = "subtasks.request.queue"
        const val QUEUE_DLQ = "dlq.queue"
        const val QUEUE_SUBTASKS_RESULT = "subtasks.result.queue"
        const val QUEUE_DLQ_NOTIFICATION = "dlq.notification.queue"

        const val ROUTING_SUBTASKS_REQUEST = "subtasks.request.routing"
        const val ROUTING_DLQ = "dlq.routing"
        const val ROUTING_SUBTASKS_RESULT = "subtasks.result.routing"
        const val ROUTING_DLQ_NOTIFICATION = "dlq.notification.routing"
    }

    @Bean
    fun subTasksRequestExchange(): DirectExchange = DirectExchange(EXCHANGE_SUBTASKS_REQUEST, true, false)
    @Bean
    fun dlqExchange(): DirectExchange = DirectExchange(EXCHANGE_DLQ, true, false)
    @Bean
    fun subTasksResultExchange(): DirectExchange = DirectExchange(EXCHANGE_SUBTASKS_RESULT, true, false)
    @Bean
    fun dlqNotificationExchange(): DirectExchange = DirectExchange(EXCHANGE_DLQ_NOTIFICATION, true, false)

    @Bean
    fun subTasksRequestQueue(): Queue {
        return QueueBuilder.durable(QUEUE_SUBTASKS_REQUEST)
            .withArgument("x-dead-letter-exchange", EXCHANGE_DLQ)
            .withArgument("x-dead-letter-routing-key", ROUTING_DLQ)
            .withArgument("x-max-retries", 3)
            .build()
    }
    @Bean
    fun dlqQueue(): Queue = Queue(QUEUE_DLQ, true)
    @Bean
    fun subTasksResultQueue(): Queue {
        return QueueBuilder.durable(QUEUE_SUBTASKS_RESULT)
            .withArgument("x-dead-letter-exchange", EXCHANGE_DLQ)
            .withArgument("x-dead-letter-routing-key", ROUTING_DLQ)
            .build()
    }
    @Bean
    fun dlqNotificationQueue(): Queue {
        return QueueBuilder.durable(QUEUE_DLQ_NOTIFICATION)
            .withArgument("x-dead-letter-exchange", EXCHANGE_DLQ)
            .withArgument("x-dead-letter-routing-key", ROUTING_DLQ)
            .build()
    }

    @Bean
    fun subTasksRequestBinding(subTasksRequestQueue: Queue, subTasksRequestExchange: DirectExchange): Binding = BindingBuilder
        .bind(subTasksRequestQueue)
        .to(subTasksRequestExchange)
        .with(ROUTING_SUBTASKS_REQUEST)
    @Bean
    fun dlqBinding(dlqQueue: Queue, dlqExchange: DirectExchange): Binding = BindingBuilder
        .bind(dlqQueue)
        .to(dlqExchange)
        .with(ROUTING_DLQ)
    @Bean
    fun subTasksResultBinding(subTasksResultQueue: Queue, subTasksResultExchange: DirectExchange): Binding = BindingBuilder
        .bind(subTasksResultQueue)
        .to(subTasksResultExchange)
        .with(ROUTING_SUBTASKS_RESULT)
    @Bean
    fun dlqNotificationBinding(dlqNotificationQueue: Queue, dlqNotificationExchange: DirectExchange): Binding = BindingBuilder
        .bind(dlqNotificationQueue)
        .to(dlqNotificationExchange)
        .with(ROUTING_DLQ_NOTIFICATION)

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val template = RabbitTemplate(connectionFactory)
        template.messageConverter = jsonConverter()
        return template
    }

    @Bean
    fun jsonConverter(): MessageConverter {
        val converter = JacksonJsonMessageConverter(JsonMapper());
        val typeMapper = DefaultJacksonJavaTypeMapper()
        typeMapper.setIdClassMapping(
            mapOf(
                "com.example.manager.services.model.SubTaskModel" to SubTaskModel::class.java
            )
        )
        converter.javaTypeMapper = typeMapper
        return converter;
    }
}