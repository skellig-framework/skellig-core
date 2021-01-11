package org.skellig.teststep.processing.model.factory

interface TestStepRegistry {

    fun getByName(testStepName: String): Map<String, Any?>?
}