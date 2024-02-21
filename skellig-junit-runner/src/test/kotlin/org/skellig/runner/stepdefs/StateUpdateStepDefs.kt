package org.skellig.runner.stepdefs

import org.skellig.teststep.runner.annotation.TestStep
import org.skellig.teststep.runner.context.SkelligTestContext
import org.skellig.teststep.runner.context.SkelligTestContextAware

class StateUpdateStepDefs : SkelligTestContextAware {

    private var context: SkelligTestContext? = null

    @TestStep(name = "The state has a default test value")
    fun checkResultAfterLog(notUsed: String?) {
        val testScenarioState = context?.getTestScenarioState()

        val values = listOf(
            mapOf(Pair("name", "Alex"), Pair("balance", 700), Pair("contact.phone", "998877")),
            mapOf(Pair("name", "Bob"), Pair("balance", 12), Pair("contact.phone", "00112233")),
            mapOf(Pair("name", "Chuck"), Pair("balance", 45))
        )

        testScenarioState?.set("test_value", values)
        testScenarioState?.set("test_value_as_map", mapOf(Pair("accounts", values)))
    }


    override fun setSkelligTestContext(context: SkelligTestContext) {
        this.context = context
    }
}