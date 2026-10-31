package org.skellig.teststep.runner.fixture.mismatch

import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.runner.annotation.TestStep

class MismatchedSteps {

    @TestStep(name = "needs (.+) and (.+)")
    fun tooFewParameters(state: TestScenarioState, a: String) {
    }
}
