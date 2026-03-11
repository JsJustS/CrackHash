package com.example.manager.controllers

import com.example.manager.controllers.dto.HashTaskStatusResponseDTO
import com.example.manager.services.TaskManagerService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping($$"${endpoint.user.status}")
class UserRequestStatusController(
    private val taskManagerService: TaskManagerService
) {

    @GetMapping
    fun getTaskStatus(
        @RequestParam requestId: UUID,
    ): ResponseEntity<HashTaskStatusResponseDTO> {
        val task = taskManagerService.getTask(requestId)
        task ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(
            HashTaskStatusResponseDTO(
                status = task.status.toString(),
                progress = task.progress,
                data = if (task.result.isEmpty()) null else task.result.toList()
            )
        )
    }
}