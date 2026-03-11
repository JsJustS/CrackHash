package com.example.manager.services

import com.example.manager.services.model.WorkerInfoModel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class WorkerManagerService {

    private val logger = LoggerFactory.getLogger(WorkerManagerService::class.java)
    private val workers = mutableListOf<WorkerInfoModel>()

    fun registerWorker(
        workerAddress: String,
        workerPort: Int
    ): WorkerInfoModel {
        val workerInfo = WorkerInfoModel(
            address = workerAddress,
            port = workerPort,
            status = WorkerStatus.ACTIVE
        )
        workers.add(workerInfo)
        logger.info("Registered worker ${workerInfo.id} with address $workerAddress:$workerPort");
        return workerInfo
    }

    fun getWorkerById(id: UUID): WorkerInfoModel? {
        return workers.firstOrNull { it.id == id }
    }

    fun getWorkers(): List<WorkerInfoModel> {
        return workers
    }

    fun removeWorker(id: UUID) {
        val workerToBeRemoved = workers.firstOrNull { it.id == id }
        workerToBeRemoved?.let {
            workers.remove(it)
            //todo: remove task and push it in queue
        }
    }
}