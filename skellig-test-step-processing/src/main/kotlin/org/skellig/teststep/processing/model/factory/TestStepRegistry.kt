package org.skellig.teststep.processing.model.factory

interface TestStepRegistry {

    fun getByName(testStepName: String): Map<Any, Any?>?

    fun getById(testStepId: String): Map<Any, Any?>?

    fun getTestSteps(): Collection<Map<Any, Any?>>
}