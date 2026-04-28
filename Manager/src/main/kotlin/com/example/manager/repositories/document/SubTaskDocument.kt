package com.example.manager.repositories.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID

@Document("subtasks")
data class SubTaskDocument (
    @Id val subTaskId: UUID = UUID.randomUUID(),
    val requestId: UUID,
    val partStart : Long,
    val partEnd : Long,
    var finished: Boolean = false,
    var sent: Boolean = false
)