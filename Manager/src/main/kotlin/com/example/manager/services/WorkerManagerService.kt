package com.example.manager.services

import com.example.manager.services.model.WorkerInfoModel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class WorkerManagerService {

    private val logger = LoggerFactory.getLogger(WorkerManagerService::class.java)
    private val workers = ConcurrentHashMap<UUID, WorkerInfoModel>()

    fun registerWorker(
        workerAddress: String,
        workerPort: Int
    ): WorkerInfoModel {
        logger.info("Registering worker $workerAddress")
        val workerInfo = WorkerInfoModel(
            address = workerAddress,
            port = workerPort,
            status = WorkerStatus.ACTIVE
        )
        workers[workerInfo.id] = workerInfo
        logger.info("Registered worker ${workerInfo.id} with address $workerAddress:8080");
        return workerInfo
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
}