package com.example.worker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
class WorkerApplication

fun main(args: Array<String>) {
	runApplication<WorkerApplication>(*args)
}
