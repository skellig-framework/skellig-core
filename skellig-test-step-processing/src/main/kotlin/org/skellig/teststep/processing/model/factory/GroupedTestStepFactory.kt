package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.GroupedTestStep
import org.skellig.teststep.processing.model.TestStep

internal class GroupedTestStepFactory(private val testStepRegistry: TestStepRegistry,
                                      private val testStepFactory: TestStepFactory<TestStep>) : TestStepFactory<GroupedTestStep> {

    companion object {
        private const val TEST = "test"
        private const val PASSED = "passed"
        private const val FAILED = "failed"
    }

    override fun create(testStepName: String, rawTestStep: Map<String, Any?>, parameters: Map<String, String?>): GroupedTestStep {
        return GroupedTestStep(rawTestStep["name"] as String, createTestStepRun(rawTestStep, parameters)!!)
    }

    private fun createTestStepRun(rawTestStep: Map<String, Any?>, parameters: Map<String, String?>): GroupedTestStep.TestStepRun? {
        var passed: GroupedTestStep.TestStepRun? = null
        var failed: GroupedTestStep.TestStepRun? = null
        if (rawTestStep.containsKey(PASSED)) {
            passed = createTestStepRun(rawTestStep[PASSED] as Map<String, Any?>, parameters)
        }
        if (rawTestStep.containsKey(FAILED)) {
            failed = createTestStepRun(rawTestStep[FAILED] as Map<String, Any?>, parameters)
        }
        return if (rawTestStep.containsKey(TEST)) {
            GroupedTestStep.TestStepRun(createConvertToTestDataFunction(rawTestStep, parameters), passed, failed)
        } else null
    }

    private fun createConvertToTestDataFunction(rawTestStep: Map<String, Any?>, parameters: Map<String, String?>): () -> TestStep =
            {
                val testStepName = rawTestStep[TEST] as String
                val rawTestStepToRun = testStepRegistry.getByName(testStepName)
                        ?: error("Test step '$testStepName' is not found in any of test data files or classes indicated in the runner")
                testStepFactory.create(testStepName, rawTestStepToRun, parameters)
            }

    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean {
        return rawTestStep.containsKey(TEST)
    }
}