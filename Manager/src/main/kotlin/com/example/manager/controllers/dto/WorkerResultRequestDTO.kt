package com.example.manager.controllers.dto

import java.util.UUID

class WorkerResultRequestDTO (
    val workerId: UUID,
    val requestId: UUID,
    val result: List<String>
)