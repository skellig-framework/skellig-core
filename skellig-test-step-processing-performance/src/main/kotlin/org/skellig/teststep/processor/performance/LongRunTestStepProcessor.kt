package org.skellig.teststep.processor.performance

import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processor.performance.model.LongRunResponse
import org.skellig.teststep.processor.performance.model.LongRunTestStep
import org.skellig.teststep.processor.performance.model.timeseries.MessageReceptionTimeSeries

open class LongRunTestStepProcessor protected constructor(
    private val testStepProcessor: TestStepProcessor<TestStep>,
    private val testStepRegistry: TestStepRegistry
) : TestStepProcessor<LongRunTestStep> {

    override fun process(testStep: LongRunTestStep): TestStepProcessor.TestStepRunResult {
        val response = LongRunResponse()

        runTestSteps(testStep.testStepsToRunBefore, response)

        val periodicRunner = PeriodicRunner(testStep, testStepRegistry)
        val timeSeries = periodicRunner.run { testStepProcessor.process(it) }

        runTestSteps(testStep.testStepsToRunAfter, response)

        response.registerTimeSeriesFor(testStep.name, timeSeries)

        val result = TestStepProcessor.TestStepRunResult(testStep)
        result.notify(response, null)
        return result
    }

    private fun runTestSteps(testSteps: List<(testStepRegistry: TestStepRegistry) -> TestStep>,
                             response : LongRunResponse) {
        testSteps.forEach {
            val testStep = it(testStepRegistry)
            val timeSeries = MessageReceptionTimeSeries(testStep.name)
            response.registerTimeSeriesFor(testStep.name, timeSeries)
            testStepProcessor.process(testStep)
                .subscribe { _, _, ex ->
                    if (ex == null) timeSeries.registerMessageReception()
                    else timeSeries.registerMessageFailed()
                }
        }
    }

    override fun getTestStepClass(): Class<LongRunTestStep> = LongRunTestStep::class.java


    class Builder {

        private var testStepProcessor: TestStepProcessor<TestStep>? = null
        private var testStepRegistry: TestStepRegistry? = null

        fun testStepProcessor(testStepProcessor: TestStepProcessor<TestStep>) = apply {
            this.testStepProcessor = testStepProcessor
        }

        fun testStepRegistry(testStepRegistry: TestStepRegistry) = apply {
            this.testStepRegistry = testStepRegistry
        }

        fun build(): LongRunTestStepProcessor =
            LongRunTestStepProcessor(testStepProcessor!!, testStepRegistry!!)
    }
}