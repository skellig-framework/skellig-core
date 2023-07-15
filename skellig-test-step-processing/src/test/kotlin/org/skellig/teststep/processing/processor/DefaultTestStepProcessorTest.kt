package org.skellig.teststep.processing.processor

import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.ExpectedResult
import org.skellig.teststep.processing.model.ValidationDetails
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import java.lang.RuntimeException

class DefaultTestStepProcessorTest {

    private val testScenarioState = DefaultTestScenarioState()
    private val validator = mock<TestStepResultValidator>()
    private val testStepProcessor = DefaultTestStepProcessor.Builder()
        .withTestScenarioState(testScenarioState)
        .withValidator(validator)
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
    fun testProcessStepWithValidationButNoTestIdInState() {
        val (validationDetails, testStep) = createTestStep()

        val result = testStepProcessor.process(testStep)
        var ex: RuntimeException? = null
        result.subscribe { _, _, e ->
            ex = e
        }

        assertEquals("Result from test step with id '${validationDetails.testStepId}' was not found in Test Scenario State", ex!!.message)
    }

    @Test
    fun testProcessStepWithValidation() {
        val (validationDetails, testStep) = createTestStep()

        val previousTestResult = "previous result"
        testScenarioState.set("${validationDetails.testStepId}_result", previousTestResult)

        testStepProcessor.process(testStep)

        verify(validator).validate(validationDetails.expectedResult, previousTestResult)
    }

    @Test
    fun testProcessStepWithValidationAndGettingResultLater() {
        val (validationDetails, testStep) = createTestStep()

        val previousTestResult = "previous result"
        Thread {
            Thread.sleep(500)
            testScenarioState.set("${validationDetails.testStepId}_result", previousTestResult)
        }.start()

        testStepProcessor.process(testStep)

        verify(validator).validate(validationDetails.expectedResult, previousTestResult)
    }

    @Test
    fun testProcessStepWithValidationAndGettingResultTimesOut() {
        val (validationDetails, testStep) = createTestStep()

        val previousTestResult = "previous result"
        Thread {
            Thread.sleep(testStep.timeout + 100L)
            testScenarioState.set("${validationDetails.testStepId}_result", previousTestResult)
        }.start()

        testStepProcessor.process(testStep)

        verify(validator, times(0)).validate(validationDetails.expectedResult, previousTestResult)
    }

    @Test
    fun testProcessStepWhenValidationFails() {
        val (validationDetails, testStep) = createTestStep()

        val previousTestResult = "previous result"
        testScenarioState.set("${validationDetails.testStepId}_result", previousTestResult)
        doThrow(ValidationException("oops")).whenever(validator).validate(validationDetails.expectedResult, previousTestResult)

        val result = testStepProcessor.process(testStep)
        var ex: RuntimeException? = null
        result.subscribe { _, _, e ->
            ex = e
        }

        assertEquals("oops", ex!!.message)
    }

    private fun createTestStep(): Pair<ValidationDetails, DefaultTestStep> {
        val validationDetails = ValidationDetails.Builder()
            .withTestStepId("previous test 1")
            .withExpectedResult(ExpectedResult())
            .build()
        val testStep = DefaultTestStep.DefaultTestStepBuilder()
            .withName("n1")
            .withValidationDetails(validationDetails)
            .withTimeout(1000)
            .withDelay(100)
            .build()
        return Pair(validationDetails, testStep)
    }
}