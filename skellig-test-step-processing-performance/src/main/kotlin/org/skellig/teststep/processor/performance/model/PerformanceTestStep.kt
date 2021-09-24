package org.skellig.teststep.processor.performance.model

import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import java.time.LocalTime

class PerformanceTestStep constructor(
    override val name: String,
    val rps: Int,
    val timeToRun: LocalTime,
    val testStepsToRunBefore: List<(testStepRegistry: TestStepRegistry) -> TestStep>,
    val testStepsToRunAfter: List<(testStepRegistry: TestStepRegistry) -> TestStep>,
    val testStepsToRun: List<(testStepRegistry: TestStepRegistry) -> TestStep>
) : TestStep