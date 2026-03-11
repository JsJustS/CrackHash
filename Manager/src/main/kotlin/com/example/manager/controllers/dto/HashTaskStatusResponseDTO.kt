package com.example.manager.controllers.dto

data class HashTaskStatusResponseDTO (
    val status: String,
    val progress: Double,
    val data: List<String>? = null
)