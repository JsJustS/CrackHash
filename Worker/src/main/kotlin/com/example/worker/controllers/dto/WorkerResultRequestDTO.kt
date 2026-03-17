package com.example.worker.controllers.dto

import java.util.UUID

class WorkerResultRequestDTO (
    val workerId: UUID,
    val requestId: UUID,
    val result: List<String>
)