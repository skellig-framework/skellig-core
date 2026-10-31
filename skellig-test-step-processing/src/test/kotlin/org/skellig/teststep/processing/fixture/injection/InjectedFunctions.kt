package org.skellig.teststep.processing.fixture.injection

import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.value.function.Function

class InjectedFunctions {

    @Function
    fun stateFirst(state: TestScenarioState, key: String): Any? = state.get(key)

    @Function
    fun stateInMiddle(prefix: String, state: TestScenarioState, suffix: String): String = prefix + state.get("k") + suffix

    @Function
    fun scopeLast(a: String, scope: TestScenarioScope): String = "$a-${scope.id}"

    @Function
    fun noInjection(a: String, b: String): String = a + b

    @Function
    fun anyIsNotInjected(a: Any?): Any? = a
}
