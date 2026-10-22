package org.skellig.teststep.processor.wiremock.model

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStepExecutionType

/** Mock server step; [operation] selects what it does. Supports stub registration and request journal reads. */
class WireMockTestStep private constructor(builder: Builder) : DefaultTestStep(
    builder.stepId, builder.stepName, TestStepExecutionType.SYNC,
    builder.stepTimeout, builder.stepDelay, builder.stepAttempts, builder.stepValues,
    builder.stepData, builder.stepValidation, builder.stepStateUpdaters
) {
    internal val operation: String = builder.operation ?: error("WireMock test step operation is required")

    /** Name of the target mock server; null when the step does not name one. */
    internal val server: String? = builder.server
    internal val requestFilter: StubRequest? = builder.requestFilter
    internal val unmatched: Boolean = builder.unmatched
    internal val stub: StubDefinition? = builder.stub

    internal class Builder : DefaultTestStep.Builder<WireMockTestStep>() {
        var operation: String? = null
        var server: String? = null
        var requestFilter: StubRequest? = null
        var unmatched: Boolean = false
        var stub: StubDefinition? = null

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
