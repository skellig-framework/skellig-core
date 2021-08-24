package org.skellig.performance.runner.service.controller

import org.skellig.performance.runner.service.SkelligPerformanceServiceRunner
import org.skellig.teststep.processor.performance.model.LongRunResponse
import org.skellig.teststep.runner.TestStepRunner
import org.skellig.teststep.runner.context.SkelligTestContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.Executors

@RestController
class SkelligPerformanceController {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SkelligPerformanceController::class.java)
    }

    @Autowired
    private var skelligTestContext: SkelligTestContext? = null

    @Autowired
    private var configPath: String? = null

    @Autowired
    @Qualifier("testSteps")
    private var testSteps: List<String>? = null

    private val taskExecutor: TaskExecutor
    private var runningTest: SkelligPerformancePageController.PerformanceTestDetails? = null
    private var startTime: LocalDateTime? = null
    private var lastError: Exception? = null

    init {
        taskExecutor = ThreadPoolTaskExecutor()
        taskExecutor.corePoolSize = 1
        taskExecutor.maxPoolSize = 1
        taskExecutor.setWaitForTasksToCompleteOnShutdown(false)
        taskExecutor.initialize()
    }

    @GetMapping("/report")
    fun report(): String {
        return "report"
    }

    @PostMapping("/run")
    fun run(details: SkelligPerformancePageController.PerformanceTestDetails) {
        stopCurrentRunningTest();
        val testStepRunner = skelligTestContext?.initialize(javaClass.classLoader,
                                                            testSteps ?: error("No paths to test steps were provided"),
                                                            configPath)

        runningTest = details
        startTime = LocalDateTime.now()
        lastError = null
        taskExecutor.execute {
            try {
                testStepRunner?.run(details.name, details.toMapParameters())
                    ?.subscribe { _, r, _ ->
                        val longRunResult = r as LongRunResponse
                        longRunResult.getTimeSeries().forEach {
                            stopCurrentRunningTest()
                            LOGGER.info("Start writing time series data into file '${it.key}'")
                            val file = File("${it.key}.sts")
                            it.value.consumeTimeSeriesRecords { record -> file.appendText(record) }
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
    fun stop(): SkelligPerformancePageController.PerformanceTestDetails? {
        stopCurrentRunningTest()
        return runningTest
    }

    @GetMapping("/progress/get")
    fun getProgress(): Progress? {
        return runningTest?.let {
            val timeToRun = runningTest?.timeToRun ?: "0"
            val timePassed = LocalTime.ofNanoOfDay(Duration.between(startTime, LocalDateTime.now()).toNanos())
            Progress(runningTest, timePassed.format(SkelligPerformancePageController.TIME_PATTERN) + " / $timeToRun")
        } ?: lastError?.let {
            Progress(error = "error: ${lastError?.message}")
        }
    }

    private fun stopCurrentRunningTest() {
        runningTest = null
        startTime = null
        skelligTestContext?.close()
    }

    class Progress(val test: SkelligPerformancePageController.PerformanceTestDetails? = null,
                   val time: String? = null,
                   val error: String? = null
    )
}