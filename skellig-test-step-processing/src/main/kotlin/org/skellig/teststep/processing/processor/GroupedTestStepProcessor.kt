package org.skellig.teststep.processing.processor

import org.skellig.teststep.processing.model.GroupedTestStep

internal class GroupedTestStepProcessor(private val testStepProcessor: CompositeTestStepProcessor)
    : TestStepProcessor<GroupedTestStep> {

    override fun process(testStep: GroupedTestStep): TestStepProcessor.TestStepRunResult {
        val internalResult = processInternal(testStep.testStepToRun)

        val testStepResult = TestStepProcessor.TestStepRunResult(testStep)
        testStepResult.notify(internalResult.first, internalResult.second)

        return testStepResult
    }

    private fun processInternal(testStep: GroupedTestStep.TestStepRun): Pair<Any?, RuntimeException?> {
        val result = testStepProcessor.process(testStep.testStepLazy())
        var finalResult: Any? = null
        var ex: RuntimeException? = null
        result.subscribe { _, r, e ->
            finalResult = r
            ex = e
        }

        val fullResponse = Pair(finalResult, ex)
        ex?.let {
            return testStep.failed?.let { processInternal(it) } ?: fullResponse
        } ?: run {
            return testStep.passed?.let { processInternal(it) } ?: fullResponse
        }
    }

    override fun getTestStepClass(): Class<GroupedTestStep> = GroupedTestStep::class.java
}