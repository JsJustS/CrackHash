package com.example.manager.repositories.document

import com.example.manager.services.TaskStatus
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID

@Document("tasks")
data class TaskDocument (
    @Id val requestId: UUID = UUID.randomUUID(),
    val status: TaskStatus = TaskStatus.CREATED,
    val hash : String,
    val maxLength : Int,
    val alphabet: String,
    var progress: Double = 0.0,
    val result: MutableList<String> = mutableListOf()
)