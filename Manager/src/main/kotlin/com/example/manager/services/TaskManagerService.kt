package com.example.manager.services

import com.example.manager.services.model.SubTaskModel
import com.example.manager.services.model.TaskModel
import com.example.manager.services.model.WorkerResultModel
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.pow

@Service
class TaskManagerService(
    private val workerManagerService: WorkerManagerService
) {
    private val logger = LoggerFactory.getLogger(TaskManagerService::class.java)
    private val tasks = ConcurrentHashMap<UUID, TaskModel>()
    private val queue = ConcurrentLinkedQueue<SubTaskModel>()

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
        subdivideTask(task)

        logger.info("Created task $task")
        return task
    }

    private fun subdivideTask(task: TaskModel) {
        val totalCombinations = calculateTotalCombinations(
            task.alphabet,
            task.maxLength
        )
        val workersCount = workerManagerService.getWorkers().count()
        val chunkSize = totalCombinations / workersCount

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
        val worker = workerManagerService.getWorkerById(workerResultModel.workerId)
        worker ?: return false
        val task = getTask(workerResultModel.requestId)
        task ?: return false
        val subTask = worker.currentSubTask
        subTask ?: return false

        task.result.addAll(workerResultModel.result)
        task.progress += subTask.progressAmount

        worker.currentSubTask = null
        return true
    }
}