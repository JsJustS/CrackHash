package com.example.worker.services

import com.example.worker.controllers.dto.WorkerRegistrationRequestDTO
import com.example.worker.controllers.dto.WorkerRegistrationResponseDTO
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

@Service
class IdentificationManagerService {
    private val restTemplate = RestTemplate()
    private var registeredWorkerId: AtomicReference<UUID> = AtomicReference()
    private val logger = LoggerFactory.getLogger(IdentificationManagerService::class.java)

    @Value($$"${manager.port}")
    private lateinit var managerPort: String
    @Value($$"${endpoint.worker.registration}")
    private lateinit var registrationUrl: String

    @PostConstruct
    fun onStart() {
        register()
        logger.info("Worker service started!")
    }

    fun isRegistered() = registeredWorkerId.get() != null
    fun getId() = registeredWorkerId.get()

    fun unregister() {
        registeredWorkerId.set(null)
        logger.info("Worker unregistered itself.")
    }

    fun register() {
        logger.info("Running registration...")
        val response = restTemplate.postForEntity(
            "http://manager:${managerPort}${registrationUrl}",
            WorkerRegistrationRequestDTO(true),
            WorkerRegistrationResponseDTO::class.java
        )
        if (!response.statusCode.is2xxSuccessful) {
            logger.error("Error while registering new worker: ${response.statusCode}")
            return
        }
        registeredWorkerId.set(response.body?.workerId)
        logger.info("Successfully registered with id ${registeredWorkerId.get()}")
    }
}