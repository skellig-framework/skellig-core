package org.skellig.teststep.runner.registry

import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.reader.value.expression.ValueExpression

/**
 * A cached implementation of the TestStepRegistry interface.
 * This class is responsible for caching test steps retrieved from multiple test step registries.
 * Test steps are represented as maps with [ValueExpression] keys and [ValueExpression] values.
 * The test steps are cached in a mutable map for efficient retrieval.
 *
 * @property testStepRegistries a list of test step registries to cache test steps from
 */
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

    override fun getById(testStepId: String): Map<ValueExpression, ValueExpression?>? {
        var testStep = cachedTestSteps[testStepId]
        if (testStep == null) {
            testStep = testStepRegistries.firstNotNullOfOrNull { it.getById(testStepId) }
            if (testStep != null) {
                cachedTestSteps[testStepId] = testStep
            }
        }
        return testStep
    }


    override fun getTestSteps(): Collection<Map<ValueExpression, ValueExpression?>> =
        testStepRegistries.flatMap { it.getTestSteps() }
}