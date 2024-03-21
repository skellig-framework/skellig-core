package org.skellig.runner

import org.skellig.feature.TestStep

class TestStepWrapper(val testStep: TestStep, val type: TestStepRunnerType = TestStepRunnerType.DEFAULT) {
}

enum class TestStepRunnerType {
    BEFORE,
    AFTER,
    DEFAULT
}