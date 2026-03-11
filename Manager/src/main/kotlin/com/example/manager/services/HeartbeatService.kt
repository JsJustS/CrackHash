package com.example.manager.services

import com.example.manager.services.model.WorkerInfoModel
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.Duration
import java.util.UUID

@Service
class HeartbeatService(
    private val workerManagerService: WorkerManagerService,
    private val taskManagerService: TaskManagerService
) {
    private val logger = LoggerFactory.getLogger(HeartbeatService::class.java)

    fun updateHeartbeatForWorker(id: UUID): Boolean {
        workerManagerService.getWorkerById(id)?.apply {
            lastHeartbeat = LocalDateTime.now()
            logger.info("Updated heartbeat for id: $id")
            return true
        }
        logger.warn("Worker with id $id not found")
        return false
    }

    @Value($$"${heartbeat.check.interval:30000}")
    private var heartbeatCheckInterval: Long? = null
    @Scheduled(fixedDelayString = $$"${heartbeat.check.interval:30000}")
    fun checkWorkersHeartbeat() {
        val workers = workerManagerService.getWorkers()
        logger.info("Starting heartbeat check for ${workers.size} workers (interval: ${heartbeatCheckInterval}ms)")

        val currentTime = LocalDateTime.now()
        val deadWorkers = workers.toList().filter { worker -> isWorkerDead(worker, currentTime) }
        deadWorkers.forEach { worker ->
            logger.warn("${worker.id} is dead")
            worker.status = WorkerStatus.INACTIVE
            workerManagerService.removeWorker(worker.id)
            worker.currentSubTask?.let(taskManagerService::queueSubTask)
        }
    }

    private fun isWorkerDead(worker: WorkerInfoModel, currentTime: LocalDateTime = LocalDateTime.now()): Boolean {
        if (heartbeatCheckInterval != null) {
            val timeSinceLastHeartbeat = Duration.between(worker.lastHeartbeat, currentTime)
            return timeSinceLastHeartbeat > Duration.ofMillis(heartbeatCheckInterval!!)
        }
        return false
    }
}