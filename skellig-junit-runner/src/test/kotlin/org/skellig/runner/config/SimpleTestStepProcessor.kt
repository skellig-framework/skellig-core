package org.skellig.runner.config

import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.processor.config.ConfiguredTestStepProcessorDetails
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfig
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.processing.state.TestScenarioState
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

class SimpleTestStepProcessor private constructor() : BaseTestStepProcessor<SimpleTestStepFactory.SimpleTestStep>() {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SimpleTestStepProcessor::class.java)

        val processedScopes = AtomicInteger(0)
    }

    override fun processTestStep(testStep: SimpleTestStepFactory.SimpleTestStep): Any {
        LOGGER.info("Start processing test with capture data: ${testStep.captureData}")
        processedScopes.incrementAndGet()
        return testStep.captureData
    }

    override fun getTestStepClass(): Class<*> = SimpleTestStepFactory.SimpleTestStep::class.java

    class Builder {

        fun build(): TestStepProcessor<SimpleTestStepFactory.SimpleTestStep> {
            return SimpleTestStepProcessor()
        }
    }
}

class SimpleTestStepProcessorConfig : TestStepProcessorConfig<SimpleTestStepFactory.SimpleTestStep> {
    override fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<SimpleTestStepFactory.SimpleTestStep> {
        return ConfiguredTestStepProcessorDetails(
            SimpleTestStepProcessor.Builder().build(),
            SimpleTestStepFactory(
                details.testStepRegistry,
                details.valueExpressionContextFactory
            )
        )
    }
}