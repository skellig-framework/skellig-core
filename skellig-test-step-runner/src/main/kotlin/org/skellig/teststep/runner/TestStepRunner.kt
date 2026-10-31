package org.skellig.teststep.runner

import org.skellig.teststep.processing.processor.TestStepProcessor.TestStepRunResult
import org.skellig.teststep.processing.state.TestScenarioScope

/**
 * The TestStepRunner interface defines methods for running test steps and returning their execution results.
 * The process of running a test step consists of the following actions:
 * - Find test step by name in an instance of [TestStepRegistry][org.skellig.teststep.processing.model.factory.TestStepRegistry]
 * - Convert raw data of the found test step into an instance of [TestStep][org.skellig.teststep.processing.model.TestStep]
 * - Use an appropriate [TestStepProcessor][org.skellig.teststep.processing.processor.TestStepProcessor] for execution
 * of TestStep and returning a result.
 */
interface TestStepRunner {

    /**
     * Runs a test step with the given test step name and returns the result of the execution.
     *
     * @param testStepName the name of the test step to run
     * @return the result of the test step execution
     */
    fun run(testStepName: String): TestStepRunResult

    /**
     * Executes the specified test step with the given test step name and parameters.
     *
     * @param testStepName the name of the test step to run
     * @param parameters a map of parameters to be used by the test step
     * @return the result of the test step execution
     */
    fun run(testStepName: String, parameters: Map<String, Any?>): TestStepRunResult

    /**
     * Executes the specified test step with the given test step name and parameters, in the given scope.
     * The test step reads and writes the state of the [scope]. The [scope] replaces a value of
     * [TestScenarioScope.PARAMETER_KEY] in the [parameters] if any, and the [parameters] are not modified.
     *
     * @param testStepName the name of the test step to run
     * @param parameters a map of parameters to be used by the test step
     * @param scope the scope of the scenario the test step runs in
     * @return the result of the test step execution
     */
    fun run(testStepName: String, parameters: Map<String, Any?>, scope: TestScenarioScope): TestStepRunResult =
        run(testStepName, TestScenarioScope.addTo(parameters, scope))
}
