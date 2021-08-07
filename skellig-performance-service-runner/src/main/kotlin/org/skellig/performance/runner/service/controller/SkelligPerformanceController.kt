package org.skellig.performance.runner.service.controller

import org.skellig.teststep.runner.TestStepRunner
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.Executors

@RestController
class SkelligPerformanceController {

    @Autowired
    private var testStepRunner: TestStepRunner? = null

    private val executorService = Executors.newSingleThreadExecutor()
    private var runningTest: SkelligPerformancePageController.PerformanceTestDetails? = null
    private var startTime: LocalDateTime? = null
    private var lastError : Exception? = null

    @GetMapping("/report")
    fun report(): String {
        return "report"
    }

    @PostMapping("/run")
    fun run(details: SkelligPerformancePageController.PerformanceTestDetails) {
        runningTest = details
        startTime = LocalDateTime.now()
        lastError = null
        executorService.submit {
            try {
                testStepRunner?.run(details.name, details.toMapParameters())
                    ?.subscribe { _, _, _ -> stopCurrentRunningTest() }
            } catch (ex: Exception) {
                stopCurrentRunningTest()
                lastError = ex
            }
        }
    }

    @PostMapping("/stop")
    fun stop(): SkelligPerformancePageController.PerformanceTestDetails? {
        stopCurrentRunningTest()
        return runningTest
    }

    @GetMapping("/progress/get")
    fun getProgress(): String? {
        return startTime?.let {
            val timeToRun = runningTest?.timeToRun ?: "0"
            val timePassed = LocalTime.ofNanoOfDay(Duration.between(it, LocalDateTime.now()).toNanos())
            timePassed.format(SkelligPerformancePageController.TIME_PATTERN) + " / $timeToRun"
        }?:lastError?.let{
            "error: ${lastError?.message}"
        }
    }

    private fun stopCurrentRunningTest() {
        runningTest = null
        startTime = null
    }
}