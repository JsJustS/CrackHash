package com.example.manager.configs

import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration

@Configuration
class MongoConfig : AbstractMongoClientConfiguration() {
    override fun getDatabaseName(): String {
        return "taskmanager"
    }

    override fun autoIndexCreation(): Boolean {
        return true
    }
}