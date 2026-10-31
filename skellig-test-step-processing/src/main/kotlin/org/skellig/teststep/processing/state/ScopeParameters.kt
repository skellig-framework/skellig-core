package org.skellig.teststep.processing.state

/**
 * Decides which parameters of user code methods (test step definitions, hooks and custom functions) are
 * injected by the framework with the [TestScenarioState] or [TestScenarioScope] of the running scenario.
 *
 * Only parameters of exactly one of these two types are injected. A parameter of a supertype, such as `Any`, is not.
 */
object ScopeParameters {

    /**
     * Returns true if a method parameter of this [type] is injected by the framework.
     */
    fun isInjected(type: Class<*>): Boolean = type == TestScenarioState::class.java || type == TestScenarioScope::class.java

    /**
     * Returns the value to inject into a method parameter of this [type]: the state or the scope itself.
     *
     * @throws IllegalArgumentException if the [type] is not injected, see [isInjected].
     */
    fun valueFor(type: Class<*>, scope: TestScenarioScope): Any = when (type) {
        TestScenarioState::class.java -> scope.state
        TestScenarioScope::class.java -> scope
        else -> throw IllegalArgumentException("Parameter of type '${type.name}' is not injected")
    }
}
