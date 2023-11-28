package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.ClassTestStep
import org.skellig.teststep.reader.value.expression.PatternValueExpression
import org.skellig.teststep.reader.value.expression.StringValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpressionContext
import java.lang.reflect.Method

internal class ClassTestStepFactory : TestStepFactory<ClassTestStep> {

    companion object {
        private val DEFAULT_CONTEXT = ValueExpressionContext()
    }

    override fun create(testStepName: String, rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, String?>): ClassTestStep {

        return ClassTestStep(
            rawTestStep[StringValueExpression("id")].toString(),
            (rawTestStep[StringValueExpression("testStepNamePattern")] as PatternValueExpression).pattern,
            rawTestStep[StringValueExpression("testStepDefInstance")]?.evaluate(DEFAULT_CONTEXT) ?: error("TestStepDefInstance must not be null"),
            (rawTestStep[StringValueExpression("testStepMethod")]?.evaluate(DEFAULT_CONTEXT) as Method?) ?: error("TestStepMethod must not be null"),
            testStepName,
            parameters
        )
    }

    override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean {
        return rawTestStep.containsKey(StringValueExpression("testStepNamePattern")) &&
                rawTestStep.containsKey(StringValueExpression("testStepDefInstance")) &&
                rawTestStep.containsKey(StringValueExpression("testStepMethod")) &&
                rawTestStep[StringValueExpression("testStepMethod")]?.javaClass == Method::class.java
    }

}