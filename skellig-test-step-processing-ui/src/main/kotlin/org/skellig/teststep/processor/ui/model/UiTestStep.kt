package org.skellig.teststep.processor.ui.model

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStepExecutionType

open class UiAction {
    data class Navigate(val url: String) : UiAction()
    open class NavigableElement(val target: String, val xpath: String) : UiAction()
    class Type(target: String, xpath: String, val value: String, val append: Boolean) : NavigableElement(target, xpath) {
        override fun toString() = "Type(target=$target, xpath=$xpath, value=<redacted>, append=$append)"
    }

    class Clear(target: String, xpath: String) : NavigableElement(target, xpath)
    class Select(target: String, xpath: String, val by: SelectBy, val value: String) : NavigableElement(target, xpath)
    class Check(target: String, xpath: String, val value: Boolean) : NavigableElement(target, xpath)
    class Wait(target: String, xpath: String, val condition: WaitCondition) : NavigableElement(target, xpath)
    class Click(target: String, xpath: String) : NavigableElement(target, xpath)
}

enum class WaitCondition { PRESENT, ABSENT, VISIBLE, HIDDEN, ENABLED }

enum class SelectBy { TEXT, VALUE }

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
