package org.skellig.feature.injection

import org.skellig.feature.hook.annotation.AfterTestFeature
import org.skellig.feature.hook.annotation.BeforeTestFeature
import org.skellig.feature.hook.annotation.BeforeTestScenario
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.processing.state.TestScenarioState

class InjectedHooks {

    @BeforeTestScenario(tags = ["@state"])
    fun withState(state: TestScenarioState) {
        calls["withState"] = state
    }

    @BeforeTestScenario(tags = ["@scope"])
    fun withScope(scope: TestScenarioScope) {
        calls["withScope"] = scope
    }

    @BeforeTestScenario(tags = ["@both"])
    fun withBoth(scope: TestScenarioScope, state: TestScenarioState) {
        calls["withBoth"] = listOf(scope, state)
    }

    @BeforeTestFeature(tags = ["@none"])
    fun withoutParameters() {
        calls["withoutParameters"] = true
    }

    @AfterTestFeature(tags = ["@fails"])
    fun failing(state: TestScenarioState) {
        throw IllegalStateException("hook failed with ${state.get("k")}")
    }

    companion object {
        val calls = mutableMapOf<String, Any>()
    }
}
