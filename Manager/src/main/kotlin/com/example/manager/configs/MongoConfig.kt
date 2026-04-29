package com.example.manager.configs

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.ReadPreference
import org.bson.UuidRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration
import org.springframework.data.mongodb.core.convert.MongoCustomConversions

@Configuration
class MongoConfig : AbstractMongoClientConfiguration() {
    override fun getDatabaseName(): String {
        return "taskmanager"
    }

    override fun autoIndexCreation(): Boolean {
        return true
    }

    @Value("\${spring.mongodb.uri}")
    private lateinit var uri: String;

    override fun configureClientSettings(builder: MongoClientSettings.Builder) {
        super.configureClientSettings(builder)
        builder.applyConnectionString(ConnectionString(uri))
        builder.uuidRepresentation(UuidRepresentation.STANDARD)
        builder.readPreference(ReadPreference.primaryPreferred())
    }
}