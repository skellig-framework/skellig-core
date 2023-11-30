package org.skellig.teststep.processing.processor

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.model.ClassTestStep
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.GroupedTestStep
import org.skellig.teststep.processing.model.TestStep
import java.util.regex.Pattern

class CompositeTestStepProcessorTest {

    private val compositeProcessor: CompositeTestStepProcessor = CompositeTestStepProcessor.Builder()
        .withTestScenarioState(mock())
        .build() as CompositeTestStepProcessor

    @Test
    fun testProcessWithRegisteredTestStepProcessor() {
        val testStep = mock<TestStep>()
        val testStepProcessor = mock<TestStepProcessor<TestStep>>()
        whenever(testStepProcessor.getTestStepClass()).thenReturn(testStep.javaClass)

        val testStepRunResult = mock<TestStepProcessor.TestStepRunResult>()
        whenever(testStepProcessor.process(testStep)).thenReturn(testStepRunResult)

        compositeProcessor.registerTestStepProcessor(testStepProcessor)

        assertEquals(testStepRunResult, compositeProcessor.process(testStep))
    }

    @Test
    fun testClose() {
        val testStep = mock<TestStep>()
        val testStepProcessor = mock<TestStepProcessor<TestStep>>()
        whenever(testStepProcessor.getTestStepClass()).thenReturn(testStep.javaClass)
        compositeProcessor.registerTestStepProcessor(testStepProcessor)

        compositeProcessor.close()

        verify(testStepProcessor).close()
    }

    @Test
    fun testProcessWithRegisteredProcessors() {
        val testStep = DefaultTestStep(name = "t1")

        assertNotNull(compositeProcessor.process(testStep))
        assertNotNull(compositeProcessor.process(GroupedTestStep("t1", GroupedTestStep.TestStepRun({ testStep }, null, null))))
        assertNotNull(compositeProcessor.process(ClassTestStep("i1", Pattern.compile(".+"), this, javaClass.methods[0], "n2", emptyMap())))
    }

    @Test
    fun testProcessWithNotRegisteredProcessors() {
        val testStep = mock<TestStep>()
        whenever(testStep.name).thenReturn("n1")
        val ex = assertThrows(TestStepProcessingException::class.java) {
            compositeProcessor.process(testStep)
        }

        assertEquals("No processor was found for test step 'n1'", ex.message)
    }
}