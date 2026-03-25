package com.example.worker.services

import com.example.worker.controllers.dto.WorkerRegistrationResponseDTO
import com.example.worker.controllers.dto.WorkerResultRequestDTO
import com.example.worker.services.models.SubTaskModel
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.security.MessageDigest
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

@Service
class SubTaskManagerService(
    private val identificationManagerService: IdentificationManagerService
) {
    private val restTemplate = RestTemplate()
    private val logger = LoggerFactory.getLogger(this::class.java)
    private var currentSubTask: AtomicReference<SubTaskModel> = AtomicReference()
    private val results = ConcurrentSkipListSet<String>()

    @Value($$"${manager.port}")
    private lateinit var managerPort: String
    @Value($$"${endpoint.worker.result}")
    private lateinit var resultUrl: String

    fun acceptSubTask(subTaskModel: SubTaskModel): Boolean {
        if (hasTask()) {
            logger.warn("Worker has already accepted another task.")
            return false
        }

        currentSubTask.set(subTaskModel)
        results.clear()
        if (identificationManagerService.isRegistered()) {
            startTask()
        }
        return true
    }

    fun hasTask(): Boolean {
        return currentSubTask.get() != null
    }

    fun startTask() {
        val t = Thread(
            this::executeCurrentTask,
            "task-executor-thread"
        )
        t.isDaemon = true
        t.start()
    }

    @PostConstruct
    fun onStart() {
        identificationManagerService.setTaskManager(this)
    }

    private fun executeCurrentTask() {
        if (currentSubTask.get() == null) {
            logger.warn("Worker is trying to execute a task but has not accepted anything yet.")
            return
        }

        val alphabet = currentSubTask.get()!!.alphabet.toCharArray()
        val hash = currentSubTask.get()!!.hash
        val maxLength = currentSubTask.get()!!.maxLength
        val partStart = currentSubTask.get()!!.partStart
        val partEnd = currentSubTask.get()!!.partEnd

        logger.info("Starting execution! Searching for $hash from $partStart to $partEnd")
        logger.info("With alphabet [${String(alphabet)}]")

        for (length in 1..maxLength) {
            val totalCombinations = alphabet.size.toDouble().pow(length).toLong()
            val totalCombinationsUpToLength = getTotalCombinationsUpToLength(alphabet.size, length - 1)

            val startInLength = max(0, partStart - totalCombinationsUpToLength)
            val endInLength = min(totalCombinations - 1, partEnd - totalCombinationsUpToLength)

            if (startInLength > endInLength || endInLength < 0) continue

            for (iteration in startInLength..endInLength) {
                if (!identificationManagerService.isRegistered()) {
                    logger.warn("Became unregistered while performing task, dropping...")
                    currentSubTask.set(null)
                    results.clear()
                    return
                }

                val word = generateWord(iteration, length, alphabet)
                val wordHash = md5(word)

                if ((iteration % 20000) == 0L) {
                    val percent = (iteration - startInLength) / (endInLength - startInLength).toDouble() * 100
                    logger.info("$word - $wordHash - $iteration (${percent.roundToInt()}/100%)")
                }

                if (wordHash == hash) {
                    results.add(word)
                    logger.info("$word is valid")
                }
            }
        }

        sendResultToManager()
    }

    private fun getTotalCombinationsUpToLength(alphabetSize: Int, maxLength: Int): Long {
        var total = 0L
        for (length in 1..maxLength) {
            total += alphabetSize.toDouble().pow(length).toLong()
        }
        return total
    }

    private fun generateWord(iteration: Long, length: Int, alphabet: CharArray): String {
        val word = CharArray(length)
        var remaining = iteration
        val alphabetSize = alphabet.size

        for (position in 0 until length) {
            val charIndex = (remaining % alphabetSize).toInt()
            word[position] = alphabet[charIndex]
            remaining /= alphabetSize
        }

        return String(word)
    }

    private fun md5(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun sendResultToManager() {
        try {
            val resultsToSend = results.toList()
            val subTaskId = currentSubTask.get().requestId

            currentSubTask.set(null)
            results.clear()

            val response = restTemplate.postForEntity(
                "http://manager:${managerPort}${resultUrl}",
                WorkerResultRequestDTO(
                    identificationManagerService.getId(),
                    subTaskId,
                    resultsToSend
                ),
                WorkerRegistrationResponseDTO::class.java
            )
            if (response.statusCode.is2xxSuccessful) {
                logger.info("Successfully sent results for $subTaskId")
            }
        } catch (e: HttpClientErrorException.NotFound) {
            logger.warn("Could not send result to manager", e)
            identificationManagerService.unregister()
        }
    }
}