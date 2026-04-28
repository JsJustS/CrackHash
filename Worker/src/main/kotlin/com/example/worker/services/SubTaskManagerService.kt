package com.example.worker.services

import com.example.worker.configs.RabbitMQConfig
import com.example.worker.services.models.SubTaskModel
import com.example.worker.services.models.WorkerResultModel
import com.rabbitmq.client.Channel
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.util.UUID
import kotlin.math.log
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

@Service
class SubTaskManagerService(
    private val rabbitTemplate: RabbitTemplate
) {
    private var registeredWorkerId: UUID = UUID.randomUUID()
    private val logger = LoggerFactory.getLogger(this::class.java)

    @RabbitListener(queues = [RabbitMQConfig.QUEUE_SUBTASKS_REQUEST], ackMode = "MANUAL")
    fun handleSubTask(
        subTask: SubTaskModel,
        channel: Channel,
        @Header(AmqpHeaders.DELIVERY_TAG) deliveryTag: Long,
        @Header(AmqpHeaders.REDELIVERED) redelivered: Boolean,
        message: Message
    ) {
        try {
            val result = processSubTask(subTask)
            logger.info("subtask with id \"${subTask.subTaskId}\" finished: $result")
            sendResultToManager(subTask.subTaskId, subTask.requestId, result)
            logger.info("sent results for ${subTask.subTaskId}\"")
            channel.basicAck(deliveryTag, false)
        } catch (e: Exception) {
            logger.error(e.message, e)
            val retryCount = (message.messageProperties.getHeader("x-retry-count") as? Int) ?: 0
            if (retryCount < 3) {
                // repeat
                logger.error("Sending subtask on repetition for the ${retryCount + 1} time")
                rabbitTemplate.convertAndSend(
                    message.messageProperties.receivedExchange,
                    message.messageProperties.receivedRoutingKey,
                    subTask,
                    { msg ->
                        msg.messageProperties.setHeader("x-retry-count", retryCount + 1)
                        msg
                    }
                )
                channel.basicAck(deliveryTag, false)
            } else {
                // DLQ
                logger.error("Sending subtask to DLQ")
                rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_DLQ_NOTIFICATION,
                    RabbitMQConfig.ROUTING_DLQ_NOTIFICATION,
                    subTask.subTaskId,
                    { msg ->
                        msg.messageProperties.setHeader("x-retry-count", 0)
                        msg
                    }
                )
                channel.basicNack(deliveryTag, false, false)
            }
        }
    }

    private fun processSubTask(subTask : SubTaskModel): List<String>{
        val alphabet = subTask.alphabet.toCharArray()
        val hash = subTask.hash
        val maxLength = subTask.maxLength
        val partStart = subTask.partStart
        val partEnd = subTask.partEnd

        val result : MutableList<String> = mutableListOf()

        logger.info("Starting execution! Searching for $hash from $partStart to $partEnd")
        logger.info("With alphabet [${String(alphabet)}]")

        for (length in 1..maxLength) {
            val totalCombinations = alphabet.size.toDouble().pow(length).toLong()
            val totalCombinationsUpToLength = getTotalCombinationsUpToLength(alphabet.size, length - 1)

            val startInLength = max(0, partStart - totalCombinationsUpToLength)
            val endInLength = min(totalCombinations - 1, partEnd - totalCombinationsUpToLength)

            if (startInLength > endInLength || endInLength < 0) continue

            for (iteration in startInLength..endInLength) {
                val word = generateWord(iteration, length, alphabet)
                val wordHash = md5(word)

                if ((iteration % 20000) == 0L) {
                    val percent = (iteration - startInLength) / (endInLength - startInLength).toDouble() * 100
                    logger.info("$word - $wordHash - $iteration (${percent.roundToInt()}/100%)")
                }

                if (wordHash == hash) {
                    result.add(word)
                    logger.info("$word is valid")

                    if (word == "bom") {
                        throw RuntimeException("DLQ Simulating Exception")
                    }
                }
            }
        }

        return result
    }

    private fun getTotalCombinationsUpToLength(alphabetSize: Int, maxLength: Int): Long {
        var total = 0L
        for (length in 1..maxLength) {
            total += alphabetSize.toDouble().pow(length).toLong()
        }
        return total
    }

    private fun generateWord(iteration: Long, length: Int, alphabet: CharArray): String {
        val word = CharArray(length)
        var remaining = iteration
        val alphabetSize = alphabet.size

        for (position in 0 until length) {
            val charIndex = (remaining % alphabetSize).toInt()
            word[position] = alphabet[charIndex]
            remaining /= alphabetSize
        }

        return String(word)
    }

    private fun md5(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun sendResultToManager(subTaskId : UUID, requestId : UUID, resultsToSend : List<String>) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_SUBTASKS_RESULT,
            RabbitMQConfig.ROUTING_SUBTASKS_RESULT,
            WorkerResultModel(
                registeredWorkerId,
                requestId,
                resultsToSend,
                subTaskId
            )
        )
    }
}