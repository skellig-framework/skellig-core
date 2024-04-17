package org.skellig.teststep.processor.performance.model

import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import java.time.LocalTime

/**
 * Represents a performance test step that has details for execution of a series of test steps repeatedly at a specific rate.
 *
 * @property name The name of the performance test step.
 * @property rps The number of requests per second to execute.
 * @property timeToRun The time duration for which the performance test step should run.
 * @property testStepsToRunBefore The list of test steps to run before the performance test steps.
 * @property testStepsToRunAfter The list of test steps to run after the performance test steps.
 * @property testStepsToRun The list of test steps to run repeatedly during the performance test.
 *
 * @constructor Creates a PerformanceTestStep with the given properties.
 */
class PerformanceTestStep(
    override val name: String,
    val rps: Int,
    val timeToRun: LocalTime,
    val testStepsToRunBefore: List<(testStepRegistry: TestStepRegistry) -> TestStep>,
    val testStepsToRunAfter: List<(testStepRegistry: TestStepRegistry) -> TestStep>,
    val testStepsToRun: List<(testStepRegistry: TestStepRegistry) -> TestStep>
) : TestStep