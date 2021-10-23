package org.skellig.teststep.runner.registry

import org.skellig.teststep.processing.model.factory.TestStepRegistry

internal class CachedTestStepsRegistry(private val testStepRegistries: List<TestStepRegistry>) : TestStepRegistry {

    private var cachedTestSteps: MutableMap<String, Map<String, Any?>?> = mutableMapOf()

    override fun getByName(testStepName: String): Map<String, Any?>? {
        var testStep = cachedTestSteps[testStepName]
        if (testStep == null) {
            testStep = testStepRegistries
                .mapNotNull { it.getByName(testStepName) }
                .firstOrNull()
            if (testStep != null) {
                cachedTestSteps[testStepName] = testStep
            }
        }
        return testStep
    }

    override fun getTestSteps(): Collection<Map<String, Any?>> =
        testStepRegistries.flatMap { it.getTestSteps() }
}