package org.skellig.performance.runner.junit

import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.notification.RunNotifier
import org.skellig.performance.runner.junit.annotation.SkelligPerformanceOptions
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.performance.model.LongRunResponse
import org.skellig.teststep.runner.TestStepRunner
import org.skellig.teststep.runner.context.SkelligTestContext
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.reflect.full.createInstance

open class SkelligPerformanceRunner(clazz: Class<*>) : Runner() {

    private val log = logger<SkelligPerformanceRunner>()

    private val skelligTestContext: SkelligTestContext
    private val testStepRunner: TestStepRunner
    private val testScenarioState: TestScenarioState
    private val testName: String

    init {
        log.info("Start to initialize Skellig Performance Runner")
        val skelligOptions = clazz.getDeclaredAnnotation(SkelligPerformanceOptions::class.java)
        testName = skelligOptions.testName

        val config = getConfig(skelligOptions.config)
        skelligTestContext = skelligOptions.context.createInstance()
        testStepRunner = skelligTestContext.initialize(clazz.classLoader, skelligOptions.testSteps.toList(), config)
        testScenarioState = skelligTestContext.getTestScenarioState()

        log.info("Skellig Performance Runner initialized successfully for test '${skelligOptions.testName}' " +
                "and with test steps from '${skelligOptions.testSteps.joinToString(",")}'")
    }

    override fun getDescription(): Description = Description.createTestDescription(testName, testName)

    override fun run(notifier: RunNotifier?) {
        log.info("Start to run the test '$testName'")

        skelligTestContext.use {
            testStepRunner.run(testName)
                .subscribe { _, r, _ ->
                    val longRunResult = r as LongRunResponse
                    longRunResult.getTimeSeries().forEach {
                        log.info("Start writing time series data into file '${it.key}'")

                        val file = File("${it.key}.sts")
                        it.value.consumeTimeSeriesRecords { record -> file.appendText(record) }

                        log.info("File '${it.key}' with time series data has been created")
                    }
                }
        }
    }

    private fun getConfig(config: String): String {
        val key = config.substringAfter("\${").substringBefore("}")
        return if (key.isNotEmpty()) {
            val property = System.getProperty(key, "")
            config.replace("\${$key}", property)
        } else {
            config
        }
    }

}