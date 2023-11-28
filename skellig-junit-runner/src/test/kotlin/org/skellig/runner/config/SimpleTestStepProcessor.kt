package org.skellig.runner.config

import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.processor.config.ConfiguredTestStepProcessorDetails
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfig
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processing.state.TestScenarioState
import org.slf4j.LoggerFactory

class SimpleTestStepProcessor private constructor(testScenarioState: TestScenarioState?) : BaseTestStepProcessor<SimpleTestStepFactory.SimpleTestStep>(testScenarioState!!) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SimpleTestStepProcessor::class.java)
    }

    override fun processTestStep(testStep: SimpleTestStepFactory.SimpleTestStep): Any {
        LOGGER.info("Start processing test with capture data: ${testStep.captureData}")
        return testStep.captureData
    }

    override fun getTestStepClass(): Class<*> = SimpleTestStepFactory.SimpleTestStep::class.java

    class Builder {
        private var testScenarioState: TestScenarioState? = null

        fun withTestScenarioState(testScenarioState: TestScenarioState?) = apply {
            this.testScenarioState = testScenarioState
        }

        fun build(): TestStepProcessor<SimpleTestStepFactory.SimpleTestStep> {
            return SimpleTestStepProcessor(testScenarioState)
        }
    }
}

class SimpleTestStepProcessorConfig : TestStepProcessorConfig<SimpleTestStepFactory.SimpleTestStep> {
    override fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<SimpleTestStepFactory.SimpleTestStep> {
        return ConfiguredTestStepProcessorDetails(
            SimpleTestStepProcessor.Builder()
                .withTestScenarioState(details.state)
                .build(),
            SimpleTestStepFactory(
                details.testStepRegistry,
                details.valueExpressionContextFactory
            )
        )
    }
}