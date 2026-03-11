package com.example.manager.services.model

import java.util.UUID

class SubTaskModel (
    val subTaskId: UUID = UUID.randomUUID(),
    val requestId: UUID,
    val hash: String,
    val maxLength: Int,
    val alphabet: String,
    val partStart: Long,
    val partEnd: Long,
    val progressAmount: Double
)