package com.example.manager

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication//(exclude=[MongoAutoConfiguration::class])
@EnableMongoRepositories
@EnableAsync
@EnableAutoConfiguration(exclude=[MongoAutoConfiguration::class])
class ManagerApplication

fun main(args: Array<String>) {
	runApplication<ManagerApplication>(*args)
}
