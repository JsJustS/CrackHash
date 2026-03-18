package com.example.worker.controllers

import com.example.worker.controllers.dto.SubTaskRequestDTO
import com.example.worker.services.SubTaskManagerService
import com.example.worker.services.models.SubTaskModel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping($$"${endpoint.worker.internal}")
class ManagerSubTaskRequestController(
    private val subTaskManagerService: SubTaskManagerService
) {

    @PostMapping
    fun acceptSubTask(
        @RequestBody subTaskRequestDTO: SubTaskRequestDTO
    ): ResponseEntity<Void> {
        val status = subTaskManagerService.acceptSubTask(
            SubTaskModel(
                subTaskId = subTaskRequestDTO.subTaskId,
                requestId = subTaskRequestDTO.requestId,
                hash = subTaskRequestDTO.hash,
                maxLength = subTaskRequestDTO.maxLength,
                alphabet = subTaskRequestDTO.alphabet,
                partStart = subTaskRequestDTO.partStart,
                partEnd = subTaskRequestDTO.partEnd
            )
        )
        if (status) {
            return ResponseEntity.accepted().build()
        }
        return ResponseEntity.badRequest().build()
    }
}