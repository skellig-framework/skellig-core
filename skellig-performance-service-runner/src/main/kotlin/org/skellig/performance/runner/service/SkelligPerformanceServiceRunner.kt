package org.skellig.performance.runner.service

import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processor.performance.model.LongRunResponse
import org.skellig.teststep.runner.TestStepRunner
import org.skellig.teststep.runner.context.SkelligTestContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.springframework.core.task.TaskExecutor
import java.io.File
import javax.annotation.PostConstruct
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor


@SpringBootApplication
abstract class SkelligPerformanceServiceRunner {

    companion object {
        private val LOGGER  = LoggerFactory.getLogger(SkelligPerformanceServiceRunner::class.java)
    }

    @Autowired
    private var testStepRunner: TestStepRunner? = null
    private val taskExecutor: TaskExecutor

    init {
        taskExecutor = ThreadPoolTaskExecutor()
        taskExecutor.corePoolSize = 1
        taskExecutor.maxPoolSize = 1
        taskExecutor.setWaitForTasksToCompleteOnShutdown(false)
        taskExecutor.initialize()
    }

    @PostConstruct
    fun init() {
        getTestName()?.let {
            run(it)
        }
    }

    private fun run(testName: String) {
        taskExecutor.execute {
            testStepRunner?.run(testName)
                ?.subscribe { _, r, _ ->
                    val longRunResult = r as LongRunResponse
                    longRunResult.getTimeSeries().forEach {
                        LOGGER.info("Start writing time series data into file '${it.key}'")
                        val file = File("${it.key}.sts")
                        it.value.consumeTimeSeriesRecords { record -> file.appendText(record) }
                        LOGGER.info("File '${it.key}' with time series data has been created")
                    }
                } ?: error("Failed to run '$testName' because test step runner was not initialized from the context")
        }
    }

    protected open fun getTestName(): String? = ""

    protected abstract fun getTestSteps(): Array<String>

    protected abstract fun getConfigFileName(): String

    protected abstract fun getContext(): SkelligTestContext

    private fun getConfigName(config: String): String {
        val key = config.substringAfter("\${").substringBefore("}")
        if (key.isNotEmpty()) {
            val property = System.getProperty(key, "")
            return config.replace("\${$key}", property)
        } else {
            return config
        }
    }

    @Bean
    open fun context(): SkelligTestContext = getContext()

    @Bean
    @Autowired
    open fun testStepRunner(context: SkelligTestContext): TestStepRunner {
        val config = getConfigName(getConfigFileName())
        return context.initialize(javaClass.classLoader, getTestSteps().toList(), config)
    }

    @Bean
    @Autowired
    @DependsOn("testStepRunner")
    open fun testScenarioState(context: SkelligTestContext): TestScenarioState =
        context.getTestScenarioState()
}