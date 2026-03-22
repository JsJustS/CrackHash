package com.example.worker.services

import com.example.worker.controllers.dto.WorkerRegistrationRequestDTO
import com.example.worker.controllers.dto.WorkerRegistrationResponseDTO
import com.example.worker.services.models.SubTaskModel
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

@Service
class IdentificationManagerService {
    private val restTemplate = RestTemplate()
    private var registeredWorkerId: AtomicReference<UUID> = AtomicReference()
    private val logger = LoggerFactory.getLogger(IdentificationManagerService::class.java)
    private var subTaskManagerService: SubTaskManagerService? = null

    @Value($$"${manager.port}")
    private lateinit var managerPort: String
    @Value($$"${endpoint.worker.registration}")
    private lateinit var registrationUrl: String

    fun isRegistered() = registeredWorkerId.get() != null
    fun getId() = registeredWorkerId.get()

    fun unregister() {
        registeredWorkerId.set(null)
        logger.info("Worker unregistered itself.")
    }

    fun register() {
        logger.info("Running registration...")
        try {
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
            val subTaskRequestDTO = response.body?.subTask
            if (subTaskRequestDTO != null) {
                val status = subTaskManagerService?.acceptSubTask(
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
                if (status == true) {
                    logger.info("Accepted subtask ${subTaskRequestDTO.subTaskId} upon registration")
                } else {
                    logger.warn("There was an error while accepting subtask ${subTaskRequestDTO.subTaskId} upon registration. Unregistering...")
                    unregister()
                }
            }
        } catch (e: ResourceAccessException) {
            logger.error("Error while registering new worker: ${e.message}")
        }

    }

    fun setTaskManager(service: SubTaskManagerService) {
        subTaskManagerService = service
    }
}