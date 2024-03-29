package org.skellig.teststep.runner

import org.skellig.teststep.processing.processor.TestStepProcessor.TestStepRunResult

interface TestStepRunner {

    fun run(testStepName: String): TestStepRunResult

    fun run(testStepName: String, parameters: Map<String, Any?>): TestStepRunResult
}