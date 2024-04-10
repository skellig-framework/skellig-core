package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.util.CachedPattern
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

abstract class BaseTestStepFactory<T : TestStep>(val valueExpressionContextFactory: ValueExpressionContextFactory) : TestStepFactory<T> {

    companion object {
        @JvmStatic
        protected fun fromProperty(property: String) : AlphanumericValueExpression = AlphanumericValueExpression(property)
        protected val TEST_STEP_NAME_KEYWORD = fromProperty("name")
    }

    protected open fun <T> convertValue(value: ValueExpression?, parameters: Map<String, Any?>): T? {
        return value?.evaluate(valueExpressionContextFactory.create(parameters)) as T?
    }

    protected fun extractParametersFromTestStepName(testStepName: String, rawTestStep: Map<ValueExpression, ValueExpression?>): MutableMap<String, Any?>? {
        var parameters: MutableMap<String, Any?>? = null
        val matcher = CachedPattern.compile(getName(rawTestStep)).matcher(testStepName)
        if (matcher.find()) {
            parameters = mutableMapOf()
            for (i in 1..matcher.groupCount()) {
                val value = matcher.group(i)
                if (value.isNotEmpty()) {
                    parameters[i.toString()] = value
                }
            }
        }
        return parameters
    }

    protected open fun getName(rawTestStep: Map<ValueExpression, ValueExpression?>): String {
        // No need to evaluate as it is always the value from toString
        return rawTestStep[TEST_STEP_NAME_KEYWORD].toString()
    }


}