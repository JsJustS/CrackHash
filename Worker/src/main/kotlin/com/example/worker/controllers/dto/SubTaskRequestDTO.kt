package com.example.worker.controllers.dto

import java.util.UUID
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Запрос на запуск подзадачи брутфорса")
data class SubTaskRequestDTO(
    @Schema(
        description = "UUID конкретной подзадачи",
        example = "a96842f1-e7e8-43ae-8061-fcada68003c6",
        required = true
    )
    val subTaskId: UUID,
    @Schema(
        description = "UUID оригинальной задачи",
        example = "21391c47-9466-4832-92e2-682a48088de6",
        required = true
    )
    val requestId: UUID,
    @Schema(
        description = "MD5 хэш для взлома",
        example = "5f4dcc3b5aa765d61d8327deb882cf99",
        required = true
    )
    val hash: String,
    @Schema(
        description = "Максимальная длина искомого слова",
        example = "4",
        minimum = "1"
    )
    val maxLength: Int,
    @Schema(
        description = "Алфавит, используемый для поиска слова",
        example = "0123456789abcdefghijklmnopqrstuvwxyz",
        required = true
    )
    val alphabet: String,
    @Schema(
        description = "Начало диапазона для данного воркера",
        example = "0"
    )
    val partStart: Long,
    @Schema(
        description = "Конец диапазона для данного воркера",
        example = "1000"
    )
    val partEnd: Long
)
