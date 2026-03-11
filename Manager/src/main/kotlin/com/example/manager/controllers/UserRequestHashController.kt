package com.example.manager.controllers

import com.example.manager.controllers.dto.HashRequestDTO
import com.example.manager.controllers.dto.HashTaskApprovedResponseDTO
import com.example.manager.services.TaskManagerService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping($$"${endpoint.user.request}")
class UserRequestHashController(
    private val taskManagerService: TaskManagerService
) {

    @PostMapping
    fun approveRequest(
        @RequestBody hashRequestDTO: HashRequestDTO
    ): ResponseEntity<HashTaskApprovedResponseDTO> {
        val task = taskManagerService.createTask(
            hashRequestDTO.hash,
            hashRequestDTO.maxLength,
            $$"${hash.alphabet}"
        )
        return ResponseEntity.ok(
            HashTaskApprovedResponseDTO(
                requestId = UUID.randomUUID()
            )
        )
    }
}