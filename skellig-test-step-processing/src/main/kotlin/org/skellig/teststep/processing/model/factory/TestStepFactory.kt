package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.TestStep

interface TestStepFactory {

    fun create(testStepName: String, rawTestStep: Map<String, Any?>, parameters: Map<String, String?>): TestStep

    fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean
}