package com.example.manager.services

import com.example.manager.controllers.dto.SubTaskRequestDTO
import com.example.manager.services.model.SubTaskModel
import com.example.manager.services.model.TaskModel
import com.example.manager.services.model.WorkerInfoModel
import com.example.manager.services.model.WorkerResultModel
import org.apache.coyote.BadRequestException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.pow

@Service
class TaskManagerService(
    private val workerManagerService: WorkerManagerService
) {
    private val restTemplate = RestTemplate()
    private val logger = LoggerFactory.getLogger(TaskManagerService::class.java)
    private val tasks = ConcurrentHashMap<UUID, TaskModel>()
    private val queue = ConcurrentLinkedQueue<SubTaskModel>()

    @Value($$"${endpoint.worker.internal}")
    private lateinit var internalUrl: String

    fun createTask(
        hash: String,
        maxLength: Int,
        alphabet: String
    ): TaskModel {
        val task = TaskModel(
            hash = hash,
            maxLength = maxLength,
            alphabet = alphabet,
        )
        tasks.putIfAbsent(task.requestId, task)
        logger.info("Created task $task: hash $hash and maxLength $maxLength")
        subdivideTask(task)
        logger.info("Subdivided task!")
        sendOutSubTasks()
        return task
    }

    private fun subdivideTask(task: TaskModel) {
        val totalCombinations = calculateTotalCombinations(
            task.alphabet,
            task.maxLength
        )
        var workersCount = workerManagerService.getWorkers().count()
        logger.info("Workers count $workersCount")
        if (workersCount == 0) {
            logger.warn("No workers found! No subdivision applied (the whole task will be forced onto first worker).")
            workersCount = 1
        }
        val chunkSize = totalCombinations / workersCount
        logger.info("Chunk size $chunkSize")

        for (i in 0 until workersCount) {
            val partStart = i * chunkSize
            val partEnd = if (i == workersCount - 1) totalCombinations else (i + 1) * chunkSize
            val subtask = SubTaskModel(
                requestId = task.requestId,
                hash = task.hash,
                maxLength = task.maxLength,
                alphabet = task.alphabet,
                partStart = partStart,
                partEnd = partEnd,
                progressAmount = 100.0 / workersCount
            )
            queueSubTask(subtask)
            logger.info("Created subtask ${subtask.subTaskId}: from $partStart to $partEnd (${subtask.hash}, ${subtask.maxLength})")
        }
    }

    fun queueSubTask(subTask: SubTaskModel) {
        if (tasks[subTask.requestId] != null) {
            queue.add(subTask)
        }
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

    fun getTask(requestId: UUID): TaskModel? {
        return tasks[requestId]
    }

    fun applySubResult(workerResultModel: WorkerResultModel): Boolean {
        logger.info("Applying subResult...")
        val worker = workerManagerService.getWorkerById(workerResultModel.workerId)
        worker ?: return false
        logger.info("worker: ${worker.id}")
        val task = getTask(workerResultModel.requestId)
        task ?: return false
        logger.info("task: ${task.requestId}")
        val subTask = worker.currentSubTask
        subTask ?: return false
        logger.info("subtask: ${subTask.subTaskId}")

        task.result.addAll(workerResultModel.result)
        task.progress += subTask.progressAmount
        if (task.progress == 100.0) {
            task.status = TaskStatus.READY
        }

        worker.currentSubTask = null
        logger.info("Applied subResult for ${task.requestId} (${task.progress}/100.0)")
        return true
    }

    fun sendOutSubTasks() {
        val workers = workerManagerService.getWorkers()
        var subTask = queue.poll()
        for (worker in workers) {
            if (worker.currentSubTask == null && subTask != null) {
                val status = sendSubTaskToWorker(subTask, worker)
                if (status) {
                    subTask = queue.poll()
                }
            }
        }
    }

    fun sendSubTaskToWorker(
        subTask: SubTaskModel,
        worker: WorkerInfoModel
    ): Boolean {
        try {
            logger.info("Sending (${subTask.hash}, ${subTask.maxLength}) to ${worker.id}")
            val response = restTemplate.postForEntity(
                "http://${worker.address}:8080${internalUrl}",
                SubTaskRequestDTO(
                    subTaskId = subTask.subTaskId,
                    requestId = subTask.requestId,
                    hash = subTask.hash,
                    maxLength = subTask.maxLength,
                    alphabet = subTask.alphabet,
                    partStart = subTask.partStart,
                    partEnd = subTask.partEnd
                ),
                Void::class.java
            )
            logger.info("Sent subTask ${subTask.requestId} to ${worker.id} (${worker.port}) [${response.statusCode.value()}]")
            worker.currentSubTask = subTask
            return response.statusCode.is2xxSuccessful
        } catch (e: BadRequestException) {
            logger.error("Failed to delegate subTask: ${e.message}")
            return false
        }
    }
}