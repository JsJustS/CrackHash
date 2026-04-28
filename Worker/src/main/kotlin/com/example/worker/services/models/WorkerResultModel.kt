package com.example.worker.services.models

import java.util.UUID

class WorkerResultModel (
    var workerId: UUID,
    var requestId: UUID,
    var result: List<String>,
    var subtaskId: UUID
)