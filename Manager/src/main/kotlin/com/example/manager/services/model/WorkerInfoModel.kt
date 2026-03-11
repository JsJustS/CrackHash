package com.example.manager.services.model

import com.example.manager.services.WorkerStatus
import java.time.LocalDateTime
import java.util.UUID

class WorkerInfoModel (
    val id: UUID = UUID.randomUUID(),
    val port: Int,
    val registeredAt: LocalDateTime = LocalDateTime.now(),
    var lastHeartbeat: LocalDateTime = LocalDateTime.now(),
    var status: WorkerStatus,
    var currentSubTask: SubTaskModel? = null
)