package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.ClassTestStep
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.PatternValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpressionContext
import java.lang.reflect.Method
import java.util.regex.Pattern

/**
 * Creates a [ClassTestStep] object from raw test step data.
 *
 * @param testStepName The name of the test step.
 * @param rawTestStep The raw test step data as a map of [ValueExpression] and their corresponding values.
 * @param parameters The parameters to be applied in the test step.
 * @return The created ClassTestStep object.
 */
internal class ClassTestStepFactory : TestStepFactory<ClassTestStep> {

    companion object {
        private val idValueExpression = AlphanumericValueExpression("id")
        private val testStepNamePattern = AlphanumericValueExpression("testStepNamePattern")
        private val testStepDefInstance = AlphanumericValueExpression("testStepDefInstance")
        private val testStepMethod = AlphanumericValueExpression("testStepMethod")
    }

    override fun create(testStepName: String, rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): ClassTestStep {

        val testStepClassInstance = (rawTestStep[testStepDefInstance]?.evaluate(ValueExpressionContext.EMPTY)
            ?: error("Failed to create ClassTestStep for the test step '$testStepName'. " +
                    "No class instance found."))
        val rawMethod = rawTestStep[testStepMethod]?.evaluate(ValueExpressionContext.EMPTY)
        val method = rawMethod as? Method?

        return ClassTestStep(
            rawTestStep[idValueExpression]?.evaluate(ValueExpressionContext.EMPTY).toString(),
            (rawTestStep[testStepNamePattern] as? PatternValueExpression)?.pattern
                ?: error("Failed to create ClassTestStep for the test step '$testStepName'. " +
                        "No regex pattern found."),
            testStepClassInstance,
            method?: error("Failed to create ClassTestStep for the test step '$testStepName'. " +
                        "Expected a Method instance but found '${rawMethod?.javaClass}'"),
            testStepName,
            parameters
        )
    }

    override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean {
        return rawTestStep.containsKey(testStepNamePattern) &&
                rawTestStep.containsKey(testStepDefInstance) &&
                rawTestStep.containsKey(testStepMethod)
    }

}