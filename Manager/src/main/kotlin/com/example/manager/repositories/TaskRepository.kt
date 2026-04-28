package com.example.manager.repositories

import com.example.manager.repositories.document.TaskDocument
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.UUID

interface TaskRepository : MongoRepository<TaskDocument, UUID> {
    fun findByRequestId(taskId: UUID): List<TaskDocument>
}