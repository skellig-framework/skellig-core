package org.skellig.teststep.processor.performance.model.factory

import org.skellig.teststep.processing.exception.TestStepCreationException
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.BaseTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processor.performance.model.PerformanceTestStep
import org.skellig.teststep.reader.value.expression.*
import java.math.BigDecimal
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class PerformanceTestStepFactory(
    private val testStepFactory: TestStepFactory<TestStep>,
    valueExpressionContextFactory: ValueExpressionContextFactory
) : BaseTestStepFactory<PerformanceTestStep>(valueExpressionContextFactory) {

    companion object {
        private val RPS = fromProperty("rps")
        private val TIME_TO_RUN = fromProperty("timeToRun")
        private val BEFORE = fromProperty("before")
        private val AFTER = fromProperty("after")
        private val RUN = fromProperty("run")
        private val TIME_PATTERN = DateTimeFormatter.ofPattern("HH:mm:ss")
    }

    override fun create(testStepName: String, rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, String?>): PerformanceTestStep {
        val rps = getRps(rawTestStep, parameters)
        val timeToRun = getTimeToRun(rawTestStep, parameters)
        val before = rawTestStep[BEFORE] as ListValueExpression?
        val after = rawTestStep[AFTER] as ListValueExpression?
        val run = rawTestStep[RUN] as ListValueExpression

        val beforeList = toListOfTestStepsToRun(before?.value, testStepName, parameters, BEFORE)
        val afterList = toListOfTestStepsToRun(after?.value, testStepName, parameters, AFTER)
        val runList = toListOfTestStepsToRun(run.value, testStepName, parameters, RUN)

        return PerformanceTestStep(testStepName, rps, timeToRun, beforeList, afterList, runList)
    }

    private fun toListOfTestStepsToRun(
        rawListOfTestSteps: List<*>?, testStepName: String,
        parameters: Map<String, String?>,
        propertyName: AlphanumericValueExpression
    ): List<(testStepRegistry: TestStepRegistry) -> TestStep> =
        rawListOfTestSteps?.let {
            rawListOfTestSteps.map {
                when (it) {
                    is MapValueExpression -> createAsTestStepDelegate(getName(it.value), parameters)
                    is StringValueExpression -> createAsTestStepDelegate(it.toString(), emptyMap())
                    else -> throw TestStepCreationException(
                        "Invalid data type of '${propertyName}' in test step '${testStepName}'. " +
                                "Must have a list of test steps to run with parameters or without"
                    )
                }
            }
        } ?: emptyList()

    private fun getRps(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, String?>): Int {
        val rps = rawTestStep[RPS]
        return convertValue<BigDecimal>(rps, parameters)?.toInt()
            ?: error("Invalid RPS value '$rps' for the test '${getName(rawTestStep)}'. The value must be Int")
    }

    private fun getTimeToRun(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): LocalTime {
        val timeToRun = convertValue<String>(rawTestStep[TIME_TO_RUN], parameters)
        return LocalTime.parse(timeToRun, TIME_PATTERN)
    }

    private fun createAsTestStepDelegate(testStepName: String, parameters: Map<String, String?>): (testStepRegistry: TestStepRegistry) -> TestStep =
        { testStepRegistry: TestStepRegistry ->
            val rawTestStepToRun = testStepRegistry.getByName(testStepName)
                ?: error("Test step '$testStepName' is not found in any of test data files or classes indicated in the runner")
            testStepFactory.create(testStepName, rawTestStepToRun, parameters)
        }

    override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean =
        rawTestStep.containsKey(RPS) &&
                rawTestStep.containsKey(TIME_TO_RUN) &&
                rawTestStep.containsKey(RUN)

}