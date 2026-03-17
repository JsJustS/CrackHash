package com.example.worker.services

import com.example.worker.controllers.dto.WorkerHeartbeatRequestDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class HeartbeatService(
    private val identificationManagerService: IdentificationManagerService
) {
    private val restTemplate = RestTemplate()
    private val logger = LoggerFactory.getLogger(HeartbeatService::class.java)

    @Value($$"${manager.port}")
    private lateinit var managerPort: String
    @Value($$"${endpoint.worker.heartbeat}")
    private lateinit var heartbeatUrl: String

    @Scheduled(fixedDelayString = $$"${interval.heartbeat.send}")
    fun sendHeartbeat() {
        if (!identificationManagerService.isRegistered()) {return}
        val response = restTemplate.postForEntity(
            "http://manager:${managerPort}${heartbeatUrl}",
            WorkerHeartbeatRequestDTO(
                identificationManagerService.getId(),
            ),
            Void::class.java
        )
        logger.info("Sent heartbeat")
        if (!response.statusCode.is2xxSuccessful) {
            logger.error("Could not send heartbeat due to ${response.statusCode}")
            identificationManagerService.unregister()
        }
    }
}