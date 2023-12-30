package org.skellig.teststep.processing.processor

import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.GroupedTestStep
import org.skellig.teststep.processing.model.ValidationNode

class GroupedTestStepProcessorTest {

    private val compositeTestStepProcessor = mock<CompositeTestStepProcessor>()
    private val testStepProcessor = GroupedTestStepProcessor(compositeTestStepProcessor)

    @Test
    fun testProcessStepWhenPreviousPassed() {
        val testStep = createTestStep()
        val testStepResult1 = TestStepProcessor.TestStepRunResult(testStep)
        testStepResult1.notify("response 1", null)
        whenever(compositeTestStepProcessor.process(testStep.testStepToRun.testStepLazy())).thenReturn(testStepResult1)

        val testStepResult2 = TestStepProcessor.TestStepRunResult(testStep)
        testStepResult2.notify("response 2", null)
        whenever(compositeTestStepProcessor.process(testStep.testStepToRun.passed!!.testStepLazy())).thenReturn(testStepResult2)

        var result: Any? = null
        testStepProcessor.process(testStep).subscribe { _, r, _ ->
            result = r
        }

        assertEquals("response 2", result)
    }

    @Test
    fun testProcess2StepsWhenPreviousFailed() {
        val testStep = createTestStep()
        val testStepResult1 = TestStepProcessor.TestStepRunResult(testStep)
        testStepResult1.notify(null, ValidationException("oops"))
        whenever(compositeTestStepProcessor.process(testStep.testStepToRun.testStepLazy())).thenReturn(testStepResult1)

        val testStepResult2 = TestStepProcessor.TestStepRunResult(testStep)
        testStepResult2.notify("response 2", null)
        whenever(compositeTestStepProcessor.process(testStep.testStepToRun.failed!!.testStepLazy())).thenReturn(testStepResult2)

        val testStepResult3 = TestStepProcessor.TestStepRunResult(testStep)
        testStepResult3.notify("response 3", null)
        whenever(compositeTestStepProcessor.process(testStep.testStepToRun.failed!!.passed!!.testStepLazy())).thenReturn(testStepResult3)

        var result: Any? = null
        testStepProcessor.process(testStep).subscribe { _, r, _ ->
            result = r
        }

        assertEquals("response 3", result)
    }

    @Test
    fun testGetTesStepClass() {
        assertEquals(GroupedTestStep::class.java, testStepProcessor.getTestStepClass())
    }

    private fun createTestStep(): GroupedTestStep {
        val testStep = DefaultTestStep.DefaultTestStepBuilder()
            .withName("n1")
            .withValidationDetails(mock<ValidationNode>())
            .withTimeout(1000)
            .withDelay(100)
            .build()

        val testStep1 = DefaultTestStep(name = "t1")
        val testStep2 = DefaultTestStep(name = "t2")
        val testStep3 = DefaultTestStep(name = "t3")
        return GroupedTestStep(
            "g1", GroupedTestStep.TestStepRun(
                { testStep },
                GroupedTestStep.TestStepRun({ testStep1 }, null, null),
                GroupedTestStep.TestStepRun(
                    { testStep2 },
                    GroupedTestStep.TestStepRun({ testStep3 }, null, null),
                    null
                )
            )
        )
    }
}