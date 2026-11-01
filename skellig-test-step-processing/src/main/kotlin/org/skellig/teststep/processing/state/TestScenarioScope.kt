package org.skellig.teststep.processing.state

/**
 * Identifies the scenario a test step runs in and gives it the [TestScenarioState] of that scenario.
 *
 * The scope travels with the parameters of the test step under the reserved key [PARAMETER_KEY],
 * and the step factories copy it onto the built test step. Processors use the state of the scope of
 * the test step they process.
 *
 * The scope also refers to the [ParallelGroupContext] of the parallel group the scenario runs in, which gives the
 * step, hook and function classes their instances.
 *
 * @property id Identifies the scope in logs and reports. It is never used to look up state.
 * @property state The state of the scenario the scope belongs to.
 * @property parallelGroup The context of the parallel group the scenario runs in. The scopes of all scenarios of
 * a parallel group must refer to the same context. A scope created without one, which is only meant for code
 * outside a Skellig run, gets a new context of its own, which is not known to the Skellig context.
 */
class TestScenarioScope(
    val id: String,
    val state: TestScenarioState,
    val parallelGroup: ParallelGroupContext = ParallelGroupContext(ParallelGroupContext.DEFAULT_KEY)
) {

    override fun toString(): String = "scope($id)"

    companion object {

        /**
         * The reserved key of the step parameters which holds the [TestScenarioScope].
         */
        const val PARAMETER_KEY = "skellig.scope"

        /**
         * The id of the scope that wraps the only state of the context, when no parallel run is configured.
         */
        const val DEFAULT_ID = "default"

        /**
         * Finds the scope in the step parameters.
         *
         * @return the scope, or null if the parameters have none.
         */
        fun from(parameters: Map<String, Any?>?): TestScenarioScope? =
            parameters?.get(PARAMETER_KEY) as? TestScenarioScope

        /**
         * Fails if the name of a task variable is [PARAMETER_KEY], as the variable would replace the scope.
         */
        fun checkVariableName(name: String) {
            check(name != PARAMETER_KEY) {
                "The variable name '$name' is reserved for the scenario scope and cannot be assigned"
            }
        }

        /**
         * Returns the [parameters] without the scope. The [parameters] are not modified.
         */
        fun removeFrom(parameters: Map<String, Any?>): Map<String, Any?> =
            if (parameters.containsKey(PARAMETER_KEY)) parameters - PARAMETER_KEY else parameters

        /**
         * Returns new parameters which have the [scope] of the caller, to be passed to a child test step.
         * The scope of the caller always replaces any value of [PARAMETER_KEY] in the [parameters].
         * The [parameters] are not modified.
         * If there is no [scope], then the [parameters] are returned as they are.
         */
        fun addTo(parameters: Map<String, Any?>, scope: TestScenarioScope?): Map<String, Any?> =
            if (scope == null) parameters else parameters + (PARAMETER_KEY to scope)
    }
}
