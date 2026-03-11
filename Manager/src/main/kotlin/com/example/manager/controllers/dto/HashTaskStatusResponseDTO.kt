package com.example.manager.controllers.dto

data class HashTaskStatusResponseDTO (
    val status: String,
    val progress: Int,
    val data: List<String>? = null
)