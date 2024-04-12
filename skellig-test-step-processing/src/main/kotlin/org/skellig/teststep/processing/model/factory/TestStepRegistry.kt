package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.reader.value.expression.ValueExpression

//TODO: consider move to skellig-test-step-runner and reference a delegator
interface TestStepRegistry {

    fun getByName(testStepName: String): Map<ValueExpression, ValueExpression?>?

    fun getById(testStepId: String): Map<ValueExpression, ValueExpression?>?

    fun getTestSteps(): Collection<Map<ValueExpression, ValueExpression?>>
}