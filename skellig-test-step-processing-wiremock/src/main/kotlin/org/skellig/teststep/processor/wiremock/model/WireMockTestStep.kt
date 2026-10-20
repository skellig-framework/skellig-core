package org.skellig.teststep.processor.wiremock.model

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStepExecutionType

/** Mock server step; the concrete operations (stub, requests, reset) are added by later work. */
class WireMockTestStep private constructor(builder: Builder) : DefaultTestStep(
    builder.stepId, builder.stepName, TestStepExecutionType.SYNC,
    builder.stepTimeout, builder.stepDelay, builder.stepAttempts, builder.stepValues,
    builder.stepData, builder.stepValidation, builder.stepStateUpdaters
) {
    internal class Builder : DefaultTestStep.Builder<WireMockTestStep>() {
        val stepId get() = id
        val stepName get() = name ?: error("WireMock test step name is required")
        val stepTimeout get() = timeout
        val stepDelay get() = delay
        val stepAttempts get() = attempts
        val stepValues get() = values
        val stepData get() = testData
        val stepValidation get() = validationDetails
        val stepStateUpdaters get() = scenarioStateUpdaters

        override fun build() = WireMockTestStep(this)
    }
}
