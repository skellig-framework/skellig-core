package org.skellig.runner

import org.skellig.feature.SkelligTestEntity
import org.skellig.feature.TestStep

/**
 * The TestStepWrapper class is a wrapper class that encapsulates a [TestStep] object and has a property of [TestStepRunnerType]
 * ot identify if the test step is for running before or after test scenario, or to be run by default as part of the test scenario.
 *
 * @property testStep The [TestStep] object to be wrapped.
 * @property type The [TestStepRunnerType] that specifies the type of TestStep execution (BEFORE, AFTER, DEFAULT).
 */
class TestStepWrapper(val testStep: TestStep, val type: TestStepRunnerType = TestStepRunnerType.DEFAULT) : SkelligTestEntity {
    override fun getEntityName(): String = testStep.getEntityName()
}

/**
 * TestStepRunnerType is an enum class that represents the types of TestStep execution:
 * - BEFORE - to be run before a test scenario
 * - AFTER - to be run after a test scenario
 * - DEFAULT - to be run in a test scenario
 */
enum class TestStepRunnerType {
    BEFORE,
    AFTER,
    DEFAULT
}