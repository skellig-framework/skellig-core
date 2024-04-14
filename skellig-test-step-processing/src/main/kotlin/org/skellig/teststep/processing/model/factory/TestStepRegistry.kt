package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.reader.value.expression.ValueExpression

//TODO: consider move to skellig-test-step-runner and reference a delegator
/**
 * Interface for a test step registry.
 * A test step registry is responsible for storing and retrieving test steps.
 * Test steps are represented as maps with [ValueExpression] keys and [ValueExpression] values.
 */
interface TestStepRegistry {

    /**
     * Retrieves a test step by its name from the test step registry.
     *
     * @param testStepName the name of the test step to retrieve
     * @return a map representing the test step, with [ValueExpression] keys and [ValueExpression] values,
     *         or null if the test step is not found
     */
    fun getByName(testStepName: String): Map<ValueExpression, ValueExpression?>?

    /**
     * Retrieves a test step by its ID from the test step registry.
     * This may be useful when finding a test which is extended by another one, as extending other tests are allowed
     * by their IDs.
     *
     * @param testStepId the ID of the test step to retrieve
     * @return a map representing the test step, with [ValueExpression] keys and [ValueExpression] values,
     *         or null if the test step is not found
     */
    fun getById(testStepId: String): Map<ValueExpression, ValueExpression?>?

    /**
     *
     * Retrieves all test steps from the test step registry.
     *
     * @return a collection of maps representing the test steps, with [ValueExpression] keys and [ValueExpression] values
     */
    fun getTestSteps(): Collection<Map<ValueExpression, ValueExpression?>>
}