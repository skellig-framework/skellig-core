package org.skellig.teststep.runner.fixture.injection

import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.runner.annotation.TestStep

class InjectedSteps {

    @TestStep(name = "state first (.+)")
    fun stateFirst(state: TestScenarioState, value: String) {
    }

    @TestStep(name = "scope in the middle (.+) and (.+)")
    fun scopeInTheMiddle(a: String, scope: TestScenarioScope, b: String) {
    }

    @TestStep(name = "state and map (.+)")
    fun stateAndMap(value: String, state: TestScenarioState, parameters: Map<String, Any?>) {
    }

    @TestStep(name = "only injected")
    fun onlyInjected(scope: TestScenarioScope) {
    }

    @TestStep(name = "no scope")
    fun noScope() {
    }
}
