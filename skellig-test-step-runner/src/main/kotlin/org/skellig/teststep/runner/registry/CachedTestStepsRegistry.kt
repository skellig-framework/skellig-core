package org.skellig.teststep.runner.registry

import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.reader.value.expression.ValueExpression

internal class CachedTestStepsRegistry(private val testStepRegistries: List<TestStepRegistry>) : TestStepRegistry {

    private var cachedTestSteps: MutableMap<String, Map<ValueExpression, ValueExpression?>?> = mutableMapOf()

    override fun getByName(testStepName: String): Map<ValueExpression, ValueExpression?>? {
        var testStep = cachedTestSteps[testStepName]
        if (testStep == null) {
            testStep = testStepRegistries.firstNotNullOfOrNull { it.getByName(testStepName) }
            if (testStep != null) {
                cachedTestSteps[testStepName] = testStep
            }
        }
        return testStep
    }

    override fun getById(testStepId: String): Map<ValueExpression, ValueExpression?>? =
        testStepRegistries.firstNotNullOfOrNull { it.getById(testStepId) }

    override fun getTestSteps(): Collection<Map<ValueExpression, ValueExpression?>> =
        testStepRegistries.flatMap { it.getTestSteps() }
}