package org.skellig.runner.config

import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator

class SimpleTestStepProcessor private constructor(testScenarioState: TestScenarioState?,
                                                  validator: TestStepResultValidator?)
    : BaseTestStepProcessor<SimpleTestStepFactory.SimpleTestStep>(testScenarioState!!, validator!!, null) {

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