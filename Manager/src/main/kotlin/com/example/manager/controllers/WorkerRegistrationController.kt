package com.example.manager.controllers

import com.example.manager.controllers.dto.SubTaskRequestDTO
import com.example.manager.controllers.dto.WorkerRegistrationRequestDTO
import com.example.manager.controllers.dto.WorkerRegistrationResponseDTO
import com.example.manager.services.WorkerManagerService
import com.example.manager.services.model.SubTaskModel
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping($$"${endpoint.worker.registration}")
class WorkerRegistrationController(
    private val workerManager: WorkerManagerService
) {

    @PostMapping
    fun registerWorker(
        @RequestBody registrationRequest: WorkerRegistrationRequestDTO,
        request: HttpServletRequest
    ): ResponseEntity<WorkerRegistrationResponseDTO> {
        val workerModel = workerManager.registerWorker(
            request.remoteAddr,
            request.remotePort
        )
        var subTask: SubTaskRequestDTO? = null
        if (workerModel.currentSubTask != null) {
            val currentSubTask = workerModel.currentSubTask!!
            subTask = SubTaskRequestDTO(
                subTaskId = currentSubTask.subTaskId,
                requestId = currentSubTask.requestId,
                hash = currentSubTask.hash,
                maxLength = currentSubTask.maxLength,
                alphabet = currentSubTask.alphabet,
                partStart = currentSubTask.partStart,
                partEnd = currentSubTask.partEnd
            )
        }
        return ResponseEntity.ok(
            WorkerRegistrationResponseDTO(
                workerId = workerModel.id,
                subTask = subTask
            )
        )
    }
}