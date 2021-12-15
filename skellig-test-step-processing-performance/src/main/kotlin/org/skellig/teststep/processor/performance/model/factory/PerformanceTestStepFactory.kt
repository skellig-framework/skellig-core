package org.skellig.teststep.processor.performance.model.factory

import org.skellig.teststep.processing.exception.TestStepCreationException
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.BaseTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepFactoryValueConverter
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processor.performance.model.PerformanceTestStep
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class PerformanceTestStepFactory(
    private val testStepFactory: TestStepFactory<TestStep>,
    keywordsProperties: Properties?,
    testStepFactoryValueConverter: TestStepFactoryValueConverter
) : BaseTestStepFactory<PerformanceTestStep>(keywordsProperties, testStepFactoryValueConverter) {

    companion object {
        private const val RPS = "test.step.keyword.rps"
        private const val TIME_TO_RUN = "test.step.keyword.timeToRun"
        private const val BEFORE = "test.step.keyword.before"
        private const val AFTER = "test.step.keyword.after"
        private const val RUN = "test.step.keyword.run"
        private val TIME_PATTERN = DateTimeFormatter.ofPattern("HH:mm:ss")
    }

    override fun create(testStepName: String, rawTestStep: Map<String, Any?>, parameters: Map<String, String?>): PerformanceTestStep {
        val rps = getRps(rawTestStep, parameters)
        val timeToRun = getTimeToRun(rawTestStep, parameters)
        val before = rawTestStep[getBeforeKeyword()] as List<*>?
        val after = rawTestStep[getAfterKeyword()] as List<*>?
        val run = rawTestStep[getRunKeyword()] as List<*>

        val beforeList = toListOfTestStepsToRun(before, testStepName, parameters, getBeforeKeyword())
        val afterList = toListOfTestStepsToRun(after, testStepName, parameters, getAfterKeyword())
        val runList = toListOfTestStepsToRun(run, testStepName, parameters, getRunKeyword())

        return PerformanceTestStep(testStepName, rps, timeToRun, beforeList, afterList, runList)
    }

    private fun toListOfTestStepsToRun(rawListOfTestSteps: List<*>?, testStepName: String,
                                       parameters: Map<String, String?>, propertyName: String): List<(testStepRegistry: TestStepRegistry) -> TestStep> =
        rawListOfTestSteps?.let {
            rawListOfTestSteps.map {
                when (it) {
                    is Map<*, *> -> {
                        val rawTestStep = it as Map<String, Any?>
                        createToTestDataFunction(getName(rawTestStep), parameters)
                    }
                    is String -> createToTestDataFunction(it, emptyMap())
                    else -> throw TestStepCreationException(
                        "Invalid data type of '${propertyName}' in test step '${testStepName}'. " +
                                "Must have a list of test steps to run with parameters or without"
                    )
                }
            }
        } ?: emptyList()

    private fun getRps(rawTestStep: Map<String, Any?>, parameters: Map<String, String?>): Int {
        val rps = rawTestStep[getRpsKeyword()].toString()
        return convertValue<String>(rps, parameters)?.toInt()
            ?: error("Invalid RPS value '$rps' for the test '${getName(rawTestStep)}'. The value must be Int")
    }

    private fun getTimeToRun(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): LocalTime {
        val timeToRun = convertValue<String>(rawTestStep[getTimeToRunKeyword()].toString(), parameters)
        return LocalTime.parse(timeToRun, TIME_PATTERN)
    }

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