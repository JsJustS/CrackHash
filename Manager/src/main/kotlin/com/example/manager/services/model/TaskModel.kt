package com.example.manager.services.model

import com.example.manager.services.TaskStatus
import java.util.UUID

class TaskModel (
    val requestId: UUID = UUID.randomUUID(),
    var status: TaskStatus = TaskStatus.IN_PROGRESS,
    val hash: String,
    val maxLength: Int,
    val alphabet: String,
    var result: List<String>? = null,
    var progress: Int = 0
)