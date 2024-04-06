package org.skellig.teststep.processing.processor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.model.ClassTestStep
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TaskTestStep
import org.skellig.teststep.processing.model.TestStep
import java.util.regex.Pattern

class CompositeTestStepProcessorTest {

    private val compositeProcessor: CompositeTestStepProcessor =
        CompositeTestStepProcessor.Builder().withValueConvertDelegate { _, _ -> }.withProcessTestStepDelegate { _, _ -> mock() }.withTestScenarioState(mock()).build() as CompositeTestStepProcessor

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
        assertNotNull(compositeProcessor.process(TaskTestStep("t1", "t1", null, 0, 0, 0, null, null, null, null, null, mutableMapOf())))
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