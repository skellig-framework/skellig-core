package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.TestStep

interface TestStepFactory<T : TestStep> {

    fun create(testStepName: String, rawTestStep: Map<String, Any?>, parameters: Map<String, String?>): T

    fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean
}