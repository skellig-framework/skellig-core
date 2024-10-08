package org.skellig.teststep.processing.processor

import org.mockito.kotlin.mock
import org.mockito.Mockito.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.ValidationNode
import org.skellig.teststep.processing.state.DefaultTestScenarioState

class DefaultTestStepProcessorTest {

    private val testScenarioState = DefaultTestScenarioState()
    private val testStepProcessor = DefaultTestStepProcessor.Builder()
        .withTestScenarioState(testScenarioState)
        .build()

    @Test
    fun testProcessStepWithoutValidation() {
        val testStep = DefaultTestStep.DefaultTestStepBuilder()
            .withName("n1")
            .withId("id1")
            .build()

        val result = testStepProcessor.process(testStep)
        var isPassed = false
        result.subscribe { t, r, e ->
            assertEquals(testStep, t)
            assertNull(e)
            assertNull(r)
            isPassed = true
        }

        assertTrue(isPassed)
        assertEquals(testStep, testScenarioState.get(testStep.id))
    }

    @Test
    fun testProcessStepWithValidation() {
        val (validationDetails, testStep) = createTestStep()

        testStepProcessor.process(testStep)

        // check validation called on null result
        verify(validationDetails).validate(null)
    }

    @Test
    fun testProcessStepWithValidationWhenFails() {
        val (validationDetails, testStep) = createTestStep()
        doThrow(ValidationException::class).whenever(validationDetails).validate(null)

        var error : RuntimeException? = null
        testStepProcessor.process(testStep).subscribe{_,_,e -> error = e }

        assertEquals(ValidationException::class.java, error?.javaClass)
    }

    @Test
    fun testGetTesStepClass() {
        assertEquals(DefaultTestStep::class.java, testStepProcessor.getTestStepClass())
    }

    private fun createTestStep(): Pair<ValidationNode, DefaultTestStep> {
        val validationNode = mock<ValidationNode>()
        val testStep = DefaultTestStep.DefaultTestStepBuilder()
            .withName("n1")
            .withValidationDetails(validationNode)
            .withTimeout(1000)
            .withDelay(100)
            .build()
        return Pair(validationNode, testStep)
    }
}