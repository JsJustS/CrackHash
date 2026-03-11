package com.example.manager.controllers

import com.example.manager.controllers.dto.WorkerResultRequestDTO
import com.example.manager.services.TaskManagerService
import com.example.manager.services.WorkerManagerService
import com.example.manager.services.model.WorkerResultModel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping($$"${endpoint.worker.result}")
class WorkerTaskResultController(
    private val taskManagerService: TaskManagerService
) {

    @PostMapping
    fun acquireResultFromWorker(
        workerResultRequestDTO: WorkerResultRequestDTO
    ): ResponseEntity<Unit> {
        val status = taskManagerService.applySubResult(
            WorkerResultModel(
                requestId = workerResultRequestDTO.requestId,
                workerId = workerResultRequestDTO.workerId,
                result = workerResultRequestDTO.result,
            )
        )
        if (status) {
            return ResponseEntity.ok().build()
        }
        return ResponseEntity.notFound().build()
    }
}