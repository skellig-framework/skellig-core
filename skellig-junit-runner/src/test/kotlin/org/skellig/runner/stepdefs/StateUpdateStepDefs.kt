package org.skellig.runner.stepdefs

import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.runner.annotation.TestStep

class StateUpdateStepDefs {

    @TestStep(name = "The state has a default test value")
    fun checkResultAfterLog(testScenarioState: TestScenarioState) {
        val values = listOf(
            mapOf(Pair("name", "Alex"), Pair("balance", 700), Pair("contact.phone", "998877")),
            mapOf(Pair("name", "Bob"), Pair("balance", 12), Pair("contact.phone", "00112233")),
            mapOf(Pair("name", "Chuck"), Pair("balance", 45))
        )

        testScenarioState.set("test_value", values)
        testScenarioState.set("test_value_as_map", mapOf(Pair("accounts", values)))
    }
}
