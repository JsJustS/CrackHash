package com.example.manager.controllers

import com.example.manager.controllers.dto.WorkerRegistrationRequestDTO
import com.example.manager.controllers.dto.WorkerRegistrationResponseDTO
import com.example.manager.services.WorkerManagerService
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
        return ResponseEntity.ok(
            WorkerRegistrationResponseDTO(
                workerId = workerModel.id
            )
        )
    }
}