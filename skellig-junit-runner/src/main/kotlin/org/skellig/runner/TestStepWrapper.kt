package org.skellig.runner

import org.skellig.feature.SkelligTestEntity
import org.skellig.feature.TestStep
import org.skellig.feature.event.ExecutionSequenceType

/**
 * The TestStepWrapper class is a wrapper class that encapsulates a [TestStep] object and has a property of [TestStepRunnerType]
 * ot identify if the test step is for running before or after test scenario, or to be run by default as part of the test scenario.
 *
 * @property testStep The [TestStep] object to be wrapped.
 * @property executionSequenceType The [ExecutionSequenceType] that specifies the type of TestStep execution (BEFORE, AFTER, NORMAL).
 */
class TestStepWrapper(
    val testStep: TestStep,
    val parentFeatureId: Int,
    val parentTestScenarioId: Int?,
    val executionSequenceType: ExecutionSequenceType = ExecutionSequenceType.NORMAL
) : SkelligTestEntity {
    override fun getEntityName(): String = testStep.getEntityName()
}