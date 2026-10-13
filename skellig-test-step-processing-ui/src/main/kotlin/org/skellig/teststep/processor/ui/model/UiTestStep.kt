package org.skellig.teststep.processor.ui.model

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStepExecutionType

internal sealed class UiAction {
    data class Navigate(val url: String) : UiAction()
    data class Click(val target: String, val xpath: String) : UiAction()
}

/** Ordered browser operations, evaluated from STS parameters before execution. */
class UiTestStep private constructor(builder: Builder) : DefaultTestStep(
    builder.stepId, builder.stepName, TestStepExecutionType.SYNC,
    builder.stepTimeout, builder.stepDelay, builder.stepAttempts, builder.stepValues,
    builder.stepData, builder.stepValidation, builder.stepStateUpdaters
) {
    internal val actions = builder.actions.toList()

    internal class Builder(val actions: List<UiAction>) : DefaultTestStep.Builder<UiTestStep>() {
        val stepId get() = id
        val stepName get() = name ?: error("UI test step name is required")
        val stepTimeout get() = timeout
        val stepDelay get() = delay
        val stepAttempts get() = attempts
        val stepValues get() = values
        val stepData get() = testData
        val stepValidation get() = validationDetails
        val stepStateUpdaters get() = scenarioStateUpdaters

        override fun build(): UiTestStep {
            require(execution == TestStepExecutionType.SYNC) { "UI test steps currently require synchronous execution" }
            return UiTestStep(this)
        }
    }
}
