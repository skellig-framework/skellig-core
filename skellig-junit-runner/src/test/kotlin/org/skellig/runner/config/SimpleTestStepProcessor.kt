package org.skellig.runner.config

import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.processor.config.ConfiguredTestStepProcessorDetails
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfig
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator

class SimpleTestStepProcessor private constructor(
    testScenarioState: TestScenarioState?,
    validator: TestStepResultValidator?
) : BaseTestStepProcessor<SimpleTestStepFactory.SimpleTestStep>(testScenarioState!!, validator!!) {

    override fun processTestStep(testStep: SimpleTestStepFactory.SimpleTestStep): Any {
        return testStep.captureData
    }

    override fun getTestStepClass(): Class<*> = SimpleTestStepFactory.SimpleTestStep::class.java

    class Builder {
        private var testScenarioState: TestScenarioState? = null
        private var validator: TestStepResultValidator? = null

        fun withTestScenarioState(testScenarioState: TestScenarioState?) = apply {
            this.testScenarioState = testScenarioState
        }

        fun withValidator(validator: TestStepResultValidator?) = apply {
            this.validator = validator
        }

        fun build(): TestStepProcessor<SimpleTestStepFactory.SimpleTestStep> {
            return SimpleTestStepProcessor(testScenarioState, validator)
        }
    }
}

class SimpleTestStepProcessorConfig : TestStepProcessorConfig<SimpleTestStepFactory.SimpleTestStep> {
    override fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<SimpleTestStepFactory.SimpleTestStep> {
        return ConfiguredTestStepProcessorDetails(
            SimpleTestStepProcessor.Builder()
                .withTestScenarioState(details.state)
                .withValidator(details.validator)
                .build(),
            SimpleTestStepFactory(
                details.testStepRegistry,
                details.keywordProperties,
                details.testStepFactoryValueConverter
            )
        )
    }
}