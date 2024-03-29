package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.reader.value.expression.ValueExpression

interface TestStepFactory<T : TestStep> {

    fun create(testStepName: String, rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): T

    fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean
}