package com.example.manager.services.model

import java.util.UUID

class WorkerResultModel (
    var workerId: UUID,
    var requestId: UUID,
    var result: List<String>
)