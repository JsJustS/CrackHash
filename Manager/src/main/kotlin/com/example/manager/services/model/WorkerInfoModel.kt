package com.example.manager.services.model

import java.time.Instant
import java.util.UUID

class WorkerInfoModel (
    val id: UUID = UUID.randomUUID(),
    val address: String,
    val port: Int,
    var lastHeartbeat: Instant = Instant.now(),
    var currentSubTask: SubTaskModel? = null
)