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

class SimpleTestStepProcessor private constructor(testScenarioState: TestScenarioState?) : BaseTestStepProcessor<SimpleTestStepFactory.SimpleTestStep>(testScenarioState!!) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SimpleTestStepProcessor::class.java)

        /** The scope of each processed test step, in order of processing. Lets tests observe the scope given to steps. */
        val processedScopes = CopyOnWriteArrayList<TestScenarioScope?>()

        /** The state the processor of the latest created context was given. */
        @Volatile
        var contextState: TestScenarioState? = null
    }

    override fun processTestStep(testStep: SimpleTestStepFactory.SimpleTestStep): Any {
        LOGGER.info("Start processing test with capture data: ${testStep.captureData}")
        processedScopes.add(testStep.scope)
        return testStep.captureData
    }

    override fun getTestStepClass(): Class<*> = SimpleTestStepFactory.SimpleTestStep::class.java

    class Builder {
        private var testScenarioState: TestScenarioState? = null

        fun withTestScenarioState(testScenarioState: TestScenarioState?) = apply {
            this.testScenarioState = testScenarioState
        }

        fun build(): TestStepProcessor<SimpleTestStepFactory.SimpleTestStep> {
            contextState = testScenarioState
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