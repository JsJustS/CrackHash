package com.example.manager.controllers

import com.example.manager.controllers.dto.HashRequestDTO
import com.example.manager.controllers.dto.HashTaskApprovedResponseDTO
import com.example.manager.services.TaskManagerService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
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

    private val logger = LoggerFactory.getLogger(UserRequestHashController::class.java)
    @Value($$"${hash.alphabet}")
    private lateinit var alphabet: String

    @PostMapping
    fun approveRequest(
        @RequestBody hashRequestDTO: HashRequestDTO
    ): ResponseEntity<HashTaskApprovedResponseDTO> {
        logger.info("Got request ${hashRequestDTO.hash} with maxLength ${hashRequestDTO.maxLength}")
        val task = taskManagerService.createTask(
            hashRequestDTO.hash,
            hashRequestDTO.maxLength,
            alphabet
        )
        return ResponseEntity.ok(
            HashTaskApprovedResponseDTO(
                requestId = task.requestId
            )
        )
    }
}