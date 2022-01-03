package org.skellig.teststep.processing.model.factory

interface TestStepRegistry {

    fun getByName(testStepName: String): Map<String, Any?>?

    fun getById(testStepId: String): Map<String, Any?>?

    fun getTestSteps(): Collection<Map<String, Any?>>
}