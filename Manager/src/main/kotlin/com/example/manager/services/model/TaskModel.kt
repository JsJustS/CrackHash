package com.example.manager.services.model

import com.example.manager.services.TaskStatus
import java.util.UUID
import java.util.concurrent.ConcurrentSkipListSet

class TaskModel (
    val requestId: UUID = UUID.randomUUID(),
    var status: TaskStatus = TaskStatus.IN_PROGRESS,
    val hash: String,
    val maxLength: Int,
    val alphabet: String,
    var result: ConcurrentSkipListSet<String> = ConcurrentSkipListSet(),
    var progress: Double = 0.0
)