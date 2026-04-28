package com.example.manager.services

import com.example.manager.configs.RabbitMQConfig
import com.example.manager.repositories.SubTaskRepository
import com.example.manager.repositories.TaskRepository
import com.example.manager.repositories.document.SubTaskDocument
import com.example.manager.repositories.document.TaskDocument
import com.example.manager.services.model.SubTaskModel
import com.example.manager.services.model.WorkerResultModel
import com.rabbitmq.client.Channel
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.handler.annotation.Header
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.Int
import kotlin.String
import kotlin.math.pow

@Service
class TaskManagerService(
    private val rabbitTemplate: RabbitTemplate,
    private val taskRepository: TaskRepository,
    private val subTaskRepository: SubTaskRepository
) {
    private val logger = LoggerFactory.getLogger(TaskManagerService::class.java)

    @Value($$"${endpoint.worker.internal}")
    private lateinit var internalUrl: String
    @Value($$"${task.subdivision.size}")
    private var taskSubdivisionSize: Int? = null

    fun createTask(
        hash: String,
        maxLength: Int,
        alphabet: String
    ): TaskDocument {
        val task = TaskDocument(
            hash = hash,
            maxLength = maxLength,
            alphabet = alphabet,
        )
        val savedTask = taskRepository.save(task)
        logger.info("Created task ${savedTask.requestId}: hash $hash and maxLength $maxLength")
        logger.info("With alphabet [$alphabet]")
        subdivideAndSendOut(task)
        return task
    }

    @Async
    private fun subdivideAndSendOut(task: TaskDocument) {
        logger.info("Subdividing task ${task.requestId}...")
        val totalCombinations = calculateTotalCombinations(
            task.alphabet,
            task.maxLength
        )
        val chunkSize = totalCombinations / (taskSubdivisionSize?:1)
        logger.info("Chunk size $chunkSize")

        val subTaskDocs = mutableListOf<SubTaskDocument>()

        for (i in 0 until (taskSubdivisionSize?:1)) {
            val partStart = i * chunkSize
            val partEnd = if (i == (taskSubdivisionSize?:1) - 1) totalCombinations else (i + 1) * chunkSize
            val subtask = SubTaskDocument(
                requestId = task.requestId,
                partStart = partStart,
                partEnd = partEnd
            )
            subTaskDocs.add(subtask)
            logger.info("Created subtask ${subtask.subTaskId}: from $partStart to $partEnd (${task.hash}, ${task.maxLength})")
        }
        val savedSubTasks = subTaskRepository.saveAll(subTaskDocs)
        logger.info("Saved ${savedSubTasks.size} subtasks for task ${task.requestId}")

        taskRepository.save(task.copy(status = TaskStatus.IN_PROGRESS))

        savedSubTasks.forEach { subTask ->
            sendSubTaskToRabbit(subTask, task)
            subTaskRepository.save(subTask.copy(sent = true))
        }
        logger.info("All subtasks for task ${task.requestId} have been sent.")
    }

    private fun calculateTotalCombinations(
        alphabet: String,
        maxLength: Int
    ): Long {
        var total = 0L
        for (len in 1..maxLength) {
            total += alphabet.length.toDouble().pow(len.toDouble()).toLong()
        }
        return total
    }

    fun getTask(requestId: UUID): TaskDocument? = taskRepository.findById(requestId).orElse(null)

    @RabbitListener(queues = [RabbitMQConfig.QUEUE_DLQ_NOTIFICATION], ackMode = "MANUAL")
    fun handleDLQ(
        uuid: UUID,
        channel : Channel,
        @Header(AmqpHeaders.DELIVERY_TAG) deliveryTag: Long,
        @Header(AmqpHeaders.REDELIVERED) redelivered: Boolean,
        message: Message
    ) {
        try {
            applyDLQ(uuid)
            channel.basicAck(deliveryTag, false)
        } catch (e: Exception) {
            logger.error(e.message, e)
            channel.basicNack(deliveryTag, false, false)
        }
    }

    fun applyDLQ(uuid: UUID) {
        subTaskRepository.findById(uuid).ifPresent { subTask ->
            val task = taskRepository.findById(subTask.requestId).orElse(null)
            if (task != null) {
                taskRepository.save(
                    task.copy(status = TaskStatus.ERROR)
                )
                logger.info("Task ${task.requestId} marked as ERROR due to subtask $uuid failure")
            } else {
                logger.warn("Task not found for subtask $uuid")
            }
        }
    }

    @RabbitListener(queues = [RabbitMQConfig.QUEUE_SUBTASKS_RESULT], ackMode = "MANUAL")
    fun handleSubResult(
        workerResultModel: WorkerResultModel,
        channel: Channel,
        @Header(AmqpHeaders.DELIVERY_TAG) deliveryTag: Long,
        @Header(AmqpHeaders.REDELIVERED) redelivered: Boolean,
        message: Message
    ) {
        try {
            applySubResult(workerResultModel)
            channel.basicAck(deliveryTag, false)
        } catch (e: Exception) {
            channel.basicNack(deliveryTag, false, false)
            logger.error(e.message, e)
        }
    }

    fun applySubResult(workerResultModel: WorkerResultModel) {
        logger.info("Received result for subtask ${workerResultModel.subtaskId} from worker ${workerResultModel.workerId}")

        val subTask = subTaskRepository.findById(workerResultModel.subtaskId).orElse(null)
        if (subTask == null) {
            logger.warn("Unknown subtask ${workerResultModel.subtaskId}, ignoring...")
            return
        }
        if (subTask.finished) {
            logger.warn("Subtask ${workerResultModel.subtaskId} already finished, ignoring duplicate result.")
            return
        }

        val task = taskRepository.findById(subTask.requestId).orElse(null)
        if (task == null) {
            logger.warn("Parent task ${subTask.requestId} for subtask ${workerResultModel.subtaskId} not found!")
            return
        }

        val newResult = task.result.toMutableList().apply {
            addAll(workerResultModel.result)
        }
        val newProgress = task.progress + 100.0 / (taskSubdivisionSize?:1)
        val newStatus = if (newProgress >= 100.0) TaskStatus.READY else TaskStatus.IN_PROGRESS

        val updatedTask = task.copy(
            result = newResult,
            progress = newProgress.coerceAtMost(100.0),
            status = newStatus
        )
        taskRepository.save(updatedTask)

        subTaskRepository.save(subTask.copy(finished = true))

        logger.info("Applied result for task ${updatedTask.requestId}: progress ${updatedTask.progress}%, status=${updatedTask.status}")
        if (newStatus == TaskStatus.READY) {
            logger.info("Task ${updatedTask.requestId} is fully completed. Result size: ${updatedTask.result.size}")
        }
    }

    fun sendSubTaskToRabbit(subTask: SubTaskDocument, task: TaskDocument) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_SUBTASKS_REQUEST,
            RabbitMQConfig.ROUTING_SUBTASKS_REQUEST,
            SubTaskModel(
                subTaskId = subTask.subTaskId,
                requestId = subTask.requestId,
                hash = task.hash,
                maxLength = task.maxLength,
                alphabet = task.alphabet,
                partStart = subTask.partStart,
                partEnd = subTask.partEnd
            )
        ) { message ->
            message.messageProperties.setHeader("x-retry-count", 0)
            message
        }
        logger.info("Sent subtask ${subTask.subTaskId} to RabbitMQ")
    }
}