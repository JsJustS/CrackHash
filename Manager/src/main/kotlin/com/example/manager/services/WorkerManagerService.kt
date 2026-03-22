package com.example.manager.services

import com.example.manager.services.model.WorkerInfoModel
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class WorkerManagerService {

    private val logger = LoggerFactory.getLogger(WorkerManagerService::class.java)
    private val workers = ConcurrentHashMap<UUID, WorkerInfoModel>()
    private var taskManagerService: TaskManagerService? = null

    fun registerWorker(
        workerAddress: String,
        workerPort: Int
    ): WorkerInfoModel {
        logger.info("Registering worker $workerAddress")
        val workerInfo = WorkerInfoModel(
            address = workerAddress,
            port = workerPort
        )
        workers[workerInfo.id] = workerInfo
        workerInfo.currentSubTask = taskManagerService?.popSubTask()
        logger.info("Registered worker ${workerInfo.id} with address $workerAddress:8080");
        return workerInfo
    }

    fun setTaskManagerService(service: TaskManagerService) {
        taskManagerService = service
    }

    fun getWorkerById(id: UUID): WorkerInfoModel? {
        return workers[id]
    }

    fun getWorkers(): List<WorkerInfoModel> {
        return workers.values.toList()
    }

    fun removeWorker(id: UUID): WorkerInfoModel? {
        return workers.remove(id)
    }

    fun updateHeartbeatForWorker(id: UUID): Boolean {
        getWorkerById(id)?.apply {
            lastHeartbeat = Instant.now()
            logger.info("Updated heartbeat for id: $id")
            return true
        }
        logger.warn("Worker with id $id not found")
        return false
    }

//    @Value($$"${heartbeat.check.interval:30000}")
//    private var heartbeatCheckInterval: Long? = null
//    @Scheduled(fixedRateString = $$"${heartbeat.check.interval:30000}")
//    fun checkWorkersHeartbeat() {
//        val workers: List<WorkerInfoModel> = getWorkers()
//        logger.info("Starting heartbeat check for ${workers.size} workers (interval: ${heartbeatCheckInterval}ms)")
//        val deadWorkers = workers.toList().filter { worker -> isWorkerDead(worker, Instant.now()) }
//        deadWorkers.forEach { worker ->
//            logger.warn("${worker.id} is dead")
//            removeWorker(worker.id)
//            taskManagerService?.let { worker.currentSubTask?.let(it::queueSubTask) }
//        }
//    }
//
//    private fun isWorkerDead(worker: WorkerInfoModel, currentTime: Instant): Boolean {
//        if (heartbeatCheckInterval != null) {
//            val timeSinceLastHeartbeat = Duration.between(worker.lastHeartbeat, currentTime)
//            return timeSinceLastHeartbeat > Duration.ofMillis(heartbeatCheckInterval!!)
//        }
//        return false
//    }
}