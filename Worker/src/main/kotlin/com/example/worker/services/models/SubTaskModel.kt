package com.example.worker.services.models

import java.util.UUID

class SubTaskModel (
    val subTaskId: UUID = UUID.randomUUID(),
    val requestId: UUID,
    val hash: String,
    val maxLength: Int,
    val alphabet: String,
    val partStart: Long,
    val partEnd: Long
)
