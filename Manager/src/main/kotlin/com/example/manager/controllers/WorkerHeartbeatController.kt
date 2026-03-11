package com.example.manager.controllers

import com.example.manager.controllers.dto.WorkerHeartbeatRequestDTO
import com.example.manager.services.HeartbeatService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping($$"${worker.heartbeat.endpoint}")
class WorkerHeartbeatController(
    private val heartbeatService: HeartbeatService
) {

    @PostMapping
    fun updateHeartbeat(
        @RequestBody heartbeatRequest: WorkerHeartbeatRequestDTO
    ): ResponseEntity<Unit> {
        val updated = heartbeatService.updateHeartbeatForWorker(
            heartbeatRequest.id
        )
        if (updated) {return ResponseEntity.ok().build()}
        // Если воркер отправил heartbeat и получил 404,
        // Значит он был в летаргическом сне, и менеджер
        // посчитал его мёртвым.
        // Воркер должен обработать эту ситуацию и
        // снова зарегистрироваться как новый воркер
        return ResponseEntity.notFound().build()
    }
}