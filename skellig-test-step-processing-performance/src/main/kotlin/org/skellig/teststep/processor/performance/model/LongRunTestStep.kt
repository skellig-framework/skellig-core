package org.skellig.teststep.processor.performance.model

import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.TestStepRegistry

class LongRunTestStep constructor(
    override val name: String,
    val rps: Int,
    val timeToRun: Int,
    val testStepsToRunBefore: List<(testStepRegistry: TestStepRegistry) -> TestStep>,
    val testStepsToRunAfter: List<(testStepRegistry: TestStepRegistry) -> TestStep>,
    val testStepsToRun: List<(testStepRegistry: TestStepRegistry) -> TestStep>
) : TestStep