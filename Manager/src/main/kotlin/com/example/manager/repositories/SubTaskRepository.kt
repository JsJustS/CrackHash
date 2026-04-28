package com.example.manager.repositories

import com.example.manager.repositories.document.SubTaskDocument
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.UUID

interface SubTaskRepository : MongoRepository<SubTaskDocument, UUID> {
    fun findByRequestIdAndFinishedFalse(requestId: UUID): List<SubTaskDocument>
    fun findByFinishedFalse(): List<SubTaskDocument>
}