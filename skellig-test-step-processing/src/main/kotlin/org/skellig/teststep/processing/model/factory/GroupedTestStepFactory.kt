package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.GroupedTestStep
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.MapValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

internal class GroupedTestStepFactory(
    private val testStepRegistry: TestStepRegistry,
    private val testStepFactory: TestStepFactory<TestStep>,
    valueExpressionContextFactory: ValueExpressionContextFactory
) : BaseTestStepFactory<GroupedTestStep>(valueExpressionContextFactory) {

    companion object {
        private val TEST = AlphanumericValueExpression("test")
        private val PASSED = AlphanumericValueExpression("passed")
        private val FAILED = AlphanumericValueExpression("failed")
    }

    override fun create(testStepName: String, rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, String?>): GroupedTestStep {
        val additionalParameters: MutableMap<String, String?> = HashMap(parameters)
        val parametersFromTestName = extractParametersFromTestStepName(testStepName, rawTestStep)
        parametersFromTestName?.let {
            additionalParameters.putAll(parametersFromTestName)
        }

        return GroupedTestStep(testStepName, createTestStepRun(rawTestStep, additionalParameters)!!)
    }

    private fun createTestStepRun(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, String?>): GroupedTestStep.TestStepRun? {
        var passed: GroupedTestStep.TestStepRun? = null
        var failed: GroupedTestStep.TestStepRun? = null

        (rawTestStep[PASSED] as MapValueExpression?)?.let {
            passed = createTestStepRun(it.value, parameters)
        }

        (rawTestStep[FAILED] as MapValueExpression?)?.let {
            failed = createTestStepRun(it.value, parameters)
        }

        return if (rawTestStep.containsKey(TEST)) {

            GroupedTestStep.TestStepRun(createConvertToTestDataFunction(rawTestStep[TEST], parameters), passed, failed)
        } else null
    }

    private fun createConvertToTestDataFunction(rawTestStepName: ValueExpression?, parameters: Map<String, String?>): () -> TestStep =
        {
            val testStepName = convertValue<String>(rawTestStepName, parameters) ?: error("Test step name cannot be null from the expression '$rawTestStepName'")
            val rawTestStepToRun = testStepRegistry.getByName(testStepName)
                ?: error("Test step '$testStepName' is not found in any of test data files or classes indicated in the runner")
            testStepFactory.create(testStepName, rawTestStepToRun, parameters)
        }

    override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean {
        return rawTestStep.containsKey(TEST)
    }
}