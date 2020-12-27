package org.skellig.runner.config

import org.skellig.task.TaskUtils.Companion.runTask
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import java.util.*

class SimpleMessageTestStepProcessor private constructor(testScenarioState: TestScenarioState?,
                                                         validator: TestStepResultValidator?)
    : BaseTestStepProcessor<SimpleMessageTestStep>(testScenarioState!!, validator!!, null) {

    private var latestReceivedMessage: MutableMap<Any, Any?>? = null

    protected override fun processTestStep(testStep: SimpleMessageTestStep): Any? {
        return if (testStep.receiveFrom != null) {
            val response = runTask({ latestReceivedMessage }, { Objects.nonNull(it) }, 500, 3000)
            response!!["receivedFrom"] = testStep.receiveFrom
            response
        } else {
            latestReceivedMessage = createResponse(testStep)
            latestReceivedMessage
        }
    }

    private fun createResponse(testStep: SimpleMessageTestStep): MutableMap<Any, Any?> {
        return hashMapOf(
                Pair("originalRequest", testStep.testData),
                Pair("receivedBy", testStep.receiver),
                Pair("status", "success"))
    }

    override fun getTestStepClass(): Class<SimpleMessageTestStep> {
        return SimpleMessageTestStep::class.java
    }

    class Builder {
        private var testScenarioState: TestScenarioState? = null
        private var validator: TestStepResultValidator? = null

        fun withTestScenarioState(testScenarioState: TestScenarioState?) = apply {
            this.testScenarioState = testScenarioState
        }

        fun withValidator(validator: TestStepResultValidator?) = apply {
            this.validator = validator
        }

        fun build(): TestStepProcessor<SimpleMessageTestStep> {
            return SimpleMessageTestStepProcessor(testScenarioState, validator)
        }
    }
}