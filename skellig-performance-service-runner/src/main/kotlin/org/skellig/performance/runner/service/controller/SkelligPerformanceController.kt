package org.skellig.performance.runner.service.controller

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.skellig.performance.runner.service.model.PerformanceTestResponse
import org.skellig.teststep.processor.performance.model.LongRunResponse
import org.skellig.teststep.runner.context.SkelligTestContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


@RestController
class SkelligPerformanceController {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SkelligPerformanceController::class.java)
        private val JSON = "application/json; charset=utf-8".toMediaType()
        private const val DEFAULT_TIMEOUT = 10L
    }

    @Autowired
    private var skelligTestContext: SkelligTestContext? = null

    @Autowired
    private var configPath: String? = null

    @Autowired
    @Qualifier("testSteps")
    private var testSteps: List<String>? = null

    private val taskExecutor: TaskExecutor
    private var httpClient: OkHttpClient
    private var runningTest: SkelligPerformancePageController.PerformanceTestDetails? = null
    private var startTime: LocalDateTime? = null
    private var lastError: Exception? = null

    init {
        taskExecutor = ThreadPoolTaskExecutor()
        taskExecutor.corePoolSize = 1
        taskExecutor.maxPoolSize = 1
        taskExecutor.setWaitForTasksToCompleteOnShutdown(false)
        taskExecutor.initialize()

        httpClient = OkHttpClient.Builder()
            .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    @GetMapping("/report")
    fun report(): String {
        return "no reports are implemented yet"
    }

    @PostMapping("/run")
    @ResponseBody
    fun run(@RequestBody details: SkelligPerformancePageController.PerformanceTestDetails): List<PerformanceTestResponse> {
        val responses = mutableListOf<PerformanceTestResponse>()
        val nodes: List<String>? = getRemoteNodesIfExist()

        if (nodes?.isNotEmpty() == true) {
            val objectMapper = ObjectMapper()
            val requestAsJson = String(objectMapper.writeValueAsBytes(details), Charset.forName("utf-8"))
            val counter = CountDownLatch(nodes.size)

            nodes.forEach {
                val request: Request = Request.Builder()
                    .url("$it/run-on-node")
                    .post(requestAsJson.toRequestBody(JSON))
                    .build()

                httpClient.newCall(request).enqueue(createRequestCallback(counter, responses))
            }

            try {
                counter.await(10, TimeUnit.SECONDS)
            } catch (ex: Exception) {
                responses.add(PerformanceTestResponse(500, null, ex.message))
            }
        } else {
            runOnNode(details)
        }
        return responses
    }

    @PostMapping("/run-on-node")
    fun runOnNode(@RequestBody details: SkelligPerformancePageController.PerformanceTestDetails) {
        stopCurrentRunningTest();
        val testStepRunner = skelligTestContext?.initialize(
            javaClass.classLoader,
            testSteps ?: error("No paths to test steps were provided"),
            configPath
        )

        runningTest = details
        startTime = LocalDateTime.now()
        lastError = null

        LOGGER.info("Start to run the test '$details'")

        taskExecutor.execute {
            try {
                testStepRunner?.run(details.name, details.toMapParameters())
                    ?.subscribe { _, r, _ ->
                        val longRunResult = r as LongRunResponse
                        longRunResult.getTimeSeries().forEach {
                            stopCurrentRunningTest()

                            LOGGER.info("Start writing time series data into file '${it.key}'")

                            val file = File("${it.key}.pts")
                            it.value.consumeTimeSeriesRecords { record ->
                                LOGGER.info(record)
                                file.appendText(record)
                            }

                            LOGGER.info("File '${it.key}' with time series data has been created")
                        }
                    }
                    ?: error("Failed to run '${details.name}' because test step runner was not initialized from the context")
            } catch (ex: Exception) {
                lastError = ex
                stopCurrentRunningTest()
            }
        }
    }

    @PostMapping("/stop")
    @ResponseBody
    fun stop(): List<PerformanceTestResponse> {
        val responses = mutableListOf<PerformanceTestResponse>()
        val nodes: List<String>? = getRemoteNodesIfExist()

        if (nodes?.isNotEmpty() == true) {
            val counter = CountDownLatch(nodes.size)

            nodes.forEach {
                val request: Request = Request.Builder()
                    .url("$it/stop-node")
                    .build()

                httpClient.newCall(request).enqueue(createRequestCallback(counter, responses))
            }

            try {
                counter.await(10, TimeUnit.SECONDS)
            } catch (ex: Exception) {
                responses.add(PerformanceTestResponse(500, null, ex.message))
            }
        } else {
            stopNode()
        }
        return responses
    }

    @PostMapping("/stop-node")
    @ResponseBody
    fun stopNode(): SkelligPerformancePageController.PerformanceTestDetails? {
        stopCurrentRunningTest()
        return runningTest
    }

    @GetMapping("/progress/get")
    @ResponseBody
    fun getProgress(): Progress? {
        return runningTest?.let {
            val timeToRun = runningTest?.timeToRun ?: "0"
            val timePassed = LocalTime.ofNanoOfDay(Duration.between(startTime, LocalDateTime.now()).toNanos())
            Progress(runningTest, timePassed.format(SkelligPerformancePageController.TIME_PATTERN) + " / $timeToRun")
        } ?: lastError?.let {
            Progress(error = "error: ${lastError?.message}")
        }
    }

    private fun getRemoteNodesIfExist(): List<String>? {
        val config = skelligTestContext?.config
        var nodes: List<String>? = null
        if (config?.hasPath("performance.nodes") == true) {
            nodes = config.getAnyRef("performance.nodes") as List<String>
        }
        return nodes
    }

    private fun createRequestCallback(
        counter: CountDownLatch,
        responses: MutableList<PerformanceTestResponse>
    ) = object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            counter.countDown()
            responses.add(PerformanceTestResponse(500, null, e.message))
        }

        override fun onResponse(call: Call, response: Response) {
            counter.countDown()
            responses.add(PerformanceTestResponse(response.code, response.body.toString(), null))
        }
    }

    private fun stopCurrentRunningTest() {
        LOGGER.info("Stop running current test '$runningTest'")

        runningTest = null
        startTime = null
        skelligTestContext?.close()
    }

    class Progress(
        val test: SkelligPerformancePageController.PerformanceTestDetails? = null,
        val time: String? = null,
        val error: String? = null
    )
}