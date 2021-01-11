package org.skellig.teststep.runner

import org.skellig.teststep.processing.model.factory.TestStepRegistry

internal class DefaultTestStepsRegistry(private val testStepRegistries : List<TestStepRegistry>) : TestStepRegistry {

    override fun getByName(testStepName: String): Map<String, Any?>? {
       return testStepRegistries
                .mapNotNull { it.getByName(testStepName) }
                .firstOrNull()
    }
}