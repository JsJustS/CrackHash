package com.example.manager.controllers.dto

import java.util.UUID

data class SubTaskRequestDTO(
    val subTaskId: UUID,
    val requestId: UUID,
    val hash: String,
    val maxLength: Int,
    val alphabet: String,
    val partStart: Long,
    val partEnd: Long
)