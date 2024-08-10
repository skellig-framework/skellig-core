package org.skellig.performance.runner.junit

import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.junit.runner.notification.StoppedByUserException
import org.junit.runners.ParentRunner
import org.junit.runners.model.RunnerScheduler
import org.junit.runners.model.Statement
import org.skellig.performance.runner.junit.annotation.SkelligPerformanceOptions
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.performance.exception.PerformanceTestStepException
import org.skellig.teststep.processor.performance.model.LongRunResponse
import org.skellig.teststep.runner.TestStepRunner
import org.skellig.teststep.runner.context.SkelligTestContext
import java.io.File
import kotlin.reflect.full.createInstance

private const val PTS_EXTENSION = "pts"

/**
 * SkelligPerformanceRunner class is used to run performance tests.
 * It initializes [SkelligTestContext] by reading @[SkelligPerformanceOptions] provided in the running class.
 *
 * @param clazz The class containing the performance test methods.
 */
open class SkelligPerformanceRunner(clazz: Class<*>) : ParentRunner<PerformanceTestStepRunner>(clazz) {

    private val log = logger<SkelligPerformanceRunner>()

    private val skelligTestContext: SkelligTestContext
    private val testStepRunner: TestStepRunner
    private val testName: String
    private val children = mutableListOf<PerformanceTestStepRunner>()

    init {
        log.info("Start to initialize Skellig Performance Runner")
        val skelligOptions = clazz.getDeclaredAnnotation(SkelligPerformanceOptions::class.java)
        testName = skelligOptions.testName
        children.add(PerformanceTestStepRunner(testName))

        val config = getConfig(skelligOptions.config)
        skelligTestContext = skelligOptions.context.createInstance()
        testStepRunner = skelligTestContext.initialize(clazz.classLoader, skelligOptions.testSteps.toList(), config)

        log.info(
            "Skellig Performance Runner initialized successfully for test '${skelligOptions.testName}' " +
                    "and with test steps from '${skelligOptions.testSteps.joinToString(",")}'"
        )
    }

    override fun getDescription(): Description = Description.createTestDescription(testName, testName)

    private fun runTestStep(
        child: PerformanceTestStepRunner,
        childDescription: Description,
        notifier: RunNotifier,
    ) {
        log.info("Start to run the test '${child.testName}'")

        notifier.fireTestStarted(childDescription)
        try {
            skelligTestContext.use {
                testStepRunner.run(child.testName)
                    .subscribe { _, r, e ->
                        val longRunResult = r as? LongRunResponse
                        longRunResult?.getTimeSeries()?.forEach {
                            log.info("Start writing time series data into file '${it.key}'")

                            val file = File("${it.key}.$PTS_EXTENSION")
                            it.value.consumeTimeSeriesRecords { record -> file.appendText(record) }

                            log.info("File '${it.key}' with time series data has been created")

                            e?.let {
                                // Only PerformanceTestStepException can be thrown
                                if (e is PerformanceTestStepException)
                                    e.aggregate().forEach { ex -> fireTestFailure(notifier, childDescription, ex) }
                            }
                        }?: error("The test '${child.testName}' has been notified with no result")
                    }
            }
        } catch (e: Throwable) {
            fireTestFailure(notifier, childDescription, e)
        } finally {
            notifier.fireTestFinished(childDescription)
        }
    }

    private fun fireTestFailure(notifier: RunNotifier, childDescription: Description, e: Throwable) {
        notifier.fireTestFailure(Failure(childDescription, e))
    }

    override fun runChild(child: PerformanceTestStepRunner, notifier: RunNotifier) {
        runTestStep(child, describeChild(child), notifier)
    }

    override fun run(notifier: RunNotifier) {
        skelligTestContext.use {
            super.run(notifier)
        }
    }

    override fun getChildren(): List<PerformanceTestStepRunner> {
        return children
    }

    override fun describeChild(child: PerformanceTestStepRunner): Description {
        return Description.createTestDescription(name, child.testName, child.testName)
    }

    override fun childrenInvoker(notifier: RunNotifier): Statement? {
        return object : Statement() {
            override fun evaluate() {
                try {
                    getChildren().forEach { feature -> scheduler.schedule { runChild(feature, notifier) } }
                } catch (ex: Exception) {
                    scheduler.finished()
                }
            }
        }
    }

    private val scheduler = object : RunnerScheduler {
        override fun schedule(childStatement: Runnable?) {
            childStatement?.run()
        }

        override fun finished() {

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