package org.skellig.runner.parallelrun

import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.value.function.Function
import org.skellig.teststep.runner.annotation.TestStep

/**
 * Writes the value of a scenario into its state through user code, which gets the state injected.
 */
class StateStepDefs {

    @TestStep(name = "Store (\\w+) in the state by a class step")
    fun storeByClassStep(value: String, state: TestScenarioState) {
        state.set("classStepValue", value)
    }

    @Function
    fun storeInStateByFunction(value: String, state: TestScenarioState): String {
        state.set("functionValue", value)
        return value
    }
}
