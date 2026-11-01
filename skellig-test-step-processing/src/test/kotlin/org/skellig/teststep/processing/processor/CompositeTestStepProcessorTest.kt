package org.skellig.teststep.processing.processor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.mock
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.model.*
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.state.TestScenarioScope
import java.util.regex.Pattern

class CompositeTestStepProcessorTest {

    private val compositeProcessor: CompositeTestStepProcessor =
        CompositeTestStepProcessor.Builder().withValueConvertDelegate { _, _ -> }.withProcessTestStepDelegate { _, _ -> mock() }.build() as CompositeTestStepProcessor

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
    fun testCloseContinuesAfterFailuresAndSuppressesSubsequentFailures() {
        val firstFailure = IllegalStateException("First processor failed to close")
        val secondFailure = IllegalArgumentException("Second processor failed to close")
        val firstProcessor = mock<TestStepProcessor<TestStep>>()
        val secondProcessor = mock<TestStepProcessor<TestStep>>()
        val lastProcessor = mock<TestStepProcessor<TestStep>>()
        listOf(firstProcessor, secondProcessor, lastProcessor).forEach {
            whenever(it.getTestStepClass()).thenReturn(TestStep::class.java)
            compositeProcessor.registerTestStepProcessor(it)
        }
        doThrow(firstFailure).whenever(firstProcessor).close()
        doThrow(secondFailure).whenever(secondProcessor).close()

        val thrown = assertThrows(IllegalStateException::class.java) {
            compositeProcessor.close()
        }

        assertSame(firstFailure, thrown)
        assertEquals(1, thrown.suppressed.size)
        assertSame(secondFailure, thrown.suppressed.single())
        verify(firstProcessor).close()
        verify(secondProcessor).close()
        verify(lastProcessor).close()
    }

    @Test
    fun testProcessWithRegisteredProcessors() {
        val scope = TestScenarioScope("test", DefaultTestScenarioState())
        val testStep = DefaultTestStep(name = "t1", scope = scope)

        assertNotNull(compositeProcessor.process(testStep))
        assertNotNull(compositeProcessor.process(TaskTestStep("t1", "t1", TestStepExecutionType.SYNC, 0, 0, 0, null, null, null, null, mutableMapOf(), scope)))
        assertNotNull(compositeProcessor.process(ClassTestStep("i1", Pattern.compile(".+"), javaClass, javaClass.methods[0], "n2", emptyMap(), scope)))
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

    @Test
    fun testClassOfTestStep() {
        assertEquals(TestStep::class.java, compositeProcessor.getTestStepClass())
    }
}
