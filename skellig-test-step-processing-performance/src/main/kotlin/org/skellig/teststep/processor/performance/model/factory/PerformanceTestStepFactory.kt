package org.skellig.teststep.processor.performance.model.factory

import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processing.exception.TestStepCreationException
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.BaseTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processor.performance.model.LongRunTestStep
import java.util.*

class PerformanceTestStepFactory(
    private val testStepFactory: TestStepFactory<TestStep>,
    keywordsProperties: Properties?,
    testStepValueConverter: TestStepValueConverter?
) : BaseTestStepFactory<LongRunTestStep>(keywordsProperties, testStepValueConverter) {

    companion object {
        private const val ARGS = "test.step.keyword.args"
        private const val RPS = "test.step.keyword.rps"
        private const val TIME_TO_RUN = "test.step.keyword.timeToRun"
        private const val BEFORE = "test.step.keyword.before"
        private const val AFTER = "test.step.keyword.after"
        private const val RUN = "test.step.keyword.run"
    }

    override fun create(testStepName: String, rawTestStep: Map<String, Any?>, parameters: Map<String, String?>): LongRunTestStep {
        val rps = rawTestStep[getRpsKeyword()].toString().toInt()
        val timeToRun = rawTestStep[getTimeToRunKeyword()].toString().toInt()
        val before = rawTestStep[getBeforeKeyword()] as List<*>?
        val after = rawTestStep[getAfterKeyword()] as List<*>?
        val run = rawTestStep[getRunKeyword()] as List<*>

        val beforeList = toListOfTestStepsToRun(before, testStepName, getBeforeKeyword())
        val afterList = toListOfTestStepsToRun(after, testStepName, getAfterKeyword())
        val runList = toListOfTestStepsToRun(run, testStepName, getRunKeyword())

        return LongRunTestStep(testStepName, rps, timeToRun, beforeList, afterList, runList)
    }

    private fun toListOfTestStepsToRun(rawListOfTestSteps: List<*>?, testStepName: String, propertyName: String): List<(testStepRegistry: TestStepRegistry) -> TestStep> =
        rawListOfTestSteps?.let {
            rawListOfTestSteps.map {
                when (it) {
                    is Map<*, *> -> {
                        val rawTestStep = it as Map<String, Any?>
                        createToTestDataFunction(getName(rawTestStep), getArgsOrEmpty(rawTestStep))
                    }
                    is String -> createToTestDataFunction(it, emptyMap())
                    else -> throw TestStepCreationException(
                        "Invalid data type of '${propertyName}' in test step '${testStepName}'. " +
                                "Must have a list of test steps to run with parameters or without"
                    )
                }
            }.toList()
        } ?: emptyList()

    private fun getArgsOrEmpty(rawTestStep: Map<String, Any?>) =
        rawTestStep.getOrDefault(getKeywordName(ARGS, "args"), emptyMap<String, String?>()) as Map<String, String?>

    private fun createToTestDataFunction(testStepName: String, parameters: Map<String, String?>): (testStepRegistry: TestStepRegistry) -> TestStep =
        { testStepRegistry: TestStepRegistry ->
            val rawTestStepToRun = testStepRegistry.getByName(testStepName)
                ?: error("Test step '$testStepName' is not found in any of test data files or classes indicated in the runner")
            testStepFactory.create(testStepName, rawTestStepToRun, parameters)
        }

    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean =
        rawTestStep.containsKey(getRpsKeyword()) &&
                rawTestStep.containsKey(getTimeToRunKeyword()) &&
                rawTestStep.containsKey(getRunKeyword())

    private fun getRpsKeyword() = getKeywordName(RPS, "rps")

    private fun getTimeToRunKeyword() = getKeywordName(TIME_TO_RUN, "timeToRun")

    private fun getRunKeyword() = getKeywordName(RUN, "run")

    private fun getAfterKeyword() = getKeywordName(AFTER, "after")

    private fun getBeforeKeyword() = getKeywordName(BEFORE, "before")
}