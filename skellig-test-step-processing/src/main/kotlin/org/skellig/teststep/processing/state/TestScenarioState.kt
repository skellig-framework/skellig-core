package org.skellig.teststep.processing.state

/**
 * The test scenario state holds all information about running test steps in the test scenario.
 * If test step has an id, then its data (ex. values, payload, validations, etc.) is stored in
 * the state.
 *
 * When a test step is finished, then its result is stored on key `<test_step_id>_result`, where
 * `test_step_id` is the id of the test step.
 *
 */
interface TestScenarioState : Iterable<Pair<String, Any?>> {

    /**
     * Get value by key from the state
     */
    fun get(key: String?): Any?

    /**
     * Insert/Update value in the state
     */
    fun set(key: String?, value: Any?)

    /**
     * Remove value in the state
     */
    fun remove(key: String?)

    /**
     * Remove all data in the state. Usually called when test scenario is finished
     */
    fun clean()
}