package org.skellig.runner

import org.skellig.feature.SkelligTestEntity
import org.skellig.feature.TestStep

class TestStepWrapper(val testStep: TestStep, val type: TestStepRunnerType = TestStepRunnerType.DEFAULT) : SkelligTestEntity {
    override fun getEntityName(): String = testStep.getEntityName()
}

enum class TestStepRunnerType {
    BEFORE,
    AFTER,
    DEFAULT
}