package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.ClassTestStep
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.PatternValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpressionContext
import java.lang.reflect.Method

internal class ClassTestStepFactory : TestStepFactory<ClassTestStep> {

    companion object {
        private val idValueExpression = AlphanumericValueExpression("id")
        private val testStepNamePattern = AlphanumericValueExpression("testStepNamePattern")
        private val testStepDefInstance = AlphanumericValueExpression("testStepDefInstance")
        private val testStepMethod = AlphanumericValueExpression("testStepMethod")
    }

    override fun create(testStepName: String, rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): ClassTestStep {

        return ClassTestStep(
            rawTestStep[idValueExpression]?.evaluate(ValueExpressionContext.EMPTY).toString(),
            (rawTestStep[testStepNamePattern] as PatternValueExpression).pattern,
            rawTestStep[testStepDefInstance]?.evaluate(ValueExpressionContext.EMPTY) ?: error("TestStepDefInstance must not be null"),
            (rawTestStep[testStepMethod]?.evaluate(ValueExpressionContext.EMPTY) as Method?) ?: error("TestStepMethod must not be null"),
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