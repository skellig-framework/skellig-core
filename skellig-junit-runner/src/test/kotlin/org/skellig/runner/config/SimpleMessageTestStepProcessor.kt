package org.skellig.runner.config

import org.skellig.task.TaskUtils.Companion.runTask
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfig
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processing.model.factory.TestStepFactory
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.processor.config.ConfiguredTestStepProcessorDetails
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import java.util.*

class SimpleMessageTestStepProcessor private constructor(
    testScenarioState: TestScenarioState?,
    validator: TestStepResultValidator?
) : BaseTestStepProcessor<SimpleMessageTestStep>(testScenarioState!!, validator!!) {

    private var latestReceivedMessage: MutableMap<Any, Any?>? = null

    override fun processTestStep(testStep: SimpleMessageTestStep): Any? {
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
            Pair("status", "success")
        )
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

class SimpleMessageTestStepProcessorConfig : TestStepProcessorConfig<SimpleMessageTestStep> {
    override fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<SimpleMessageTestStep> {
        return ConfiguredTestStepProcessorDetails(
            SimpleMessageTestStepProcessor.Builder()
                .withTestScenarioState(details.state)
                .withValidator(details.validator)
                .build(),
            SimpleMessageTestStepFactory(
                details.testStepRegistry,
                details.keywordProperties,
                details.testStepFactoryValueConverter
            )
        )
    }
}