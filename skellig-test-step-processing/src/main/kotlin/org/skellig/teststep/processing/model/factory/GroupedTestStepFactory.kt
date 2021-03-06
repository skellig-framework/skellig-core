package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processing.model.GroupedTestStep
import org.skellig.teststep.processing.model.TestStep
import java.util.*
import kotlin.collections.HashMap

internal class GroupedTestStepFactory(private val testStepRegistry: TestStepRegistry,
                                      private val testStepFactory: TestStepFactory<TestStep>,
                                      keywordsProperties: Properties?,
                                      testStepValueConverter: TestStepValueConverter?)
    : BaseTestStepFactory<GroupedTestStep>(keywordsProperties, testStepValueConverter) {

    companion object {
        private const val TEST = "test"
        private const val PASSED = "passed"
        private const val FAILED = "failed"
    }

    override fun create(testStepName: String, rawTestStep: Map<String, Any?>, parameters: Map<String, String?>): GroupedTestStep {
        val additionalParameters: MutableMap<String, String?> = HashMap(parameters)
        val parametersFromTestName = extractParametersFromTestStepName(testStepName, rawTestStep)
        parametersFromTestName?.let {
            additionalParameters.putAll(parametersFromTestName)
        }

        return GroupedTestStep(testStepName, createTestStepRun(rawTestStep, additionalParameters)!!)
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
                var testStepName = rawTestStep[TEST] as String
                testStepName = testStepFactoryValueConverter.convertValue<String>(testStepName, parameters) ?: testStepName
                val rawTestStepToRun = testStepRegistry.getByName(testStepName)
                        ?: error("Test step '$testStepName' is not found in any of test data files or classes indicated in the runner")
                testStepFactory.create(testStepName, rawTestStepToRun, parameters)
            }

    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean {
        return rawTestStep.containsKey(TEST)
    }
}