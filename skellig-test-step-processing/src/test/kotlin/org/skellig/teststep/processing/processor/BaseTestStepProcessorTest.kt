package org.skellig.teststep.processing.processor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.state.TestScenarioState

private const val DEFAULT_RESULT = "processed"
private const val DEFAULT_ASYNC_DELAY = 500L

class BaseTestStepProcessorTest {

    private val testScenarioState = DefaultTestScenarioState()

    @Test
    fun testProcessAsync() {
        val processor = AsyncBaseTestStepProcessorForTest(testScenarioState)
        val testStep = DefaultTestStep.DefaultTestStepBuilder()
            .withName("n1")
            .withExecution(TestStepExecutionType.ASYNC)
            .build()

        val result = processor.process(testStep)
        var resultValue: Any? = null
        result.subscribe { _, r, _ ->
            resultValue = r
        }

        Thread.sleep(DEFAULT_ASYNC_DELAY + 100L)
        assertEquals(DEFAULT_RESULT, resultValue)
        assertEquals(DEFAULT_RESULT, testScenarioState.get(testStep.getId + TestStepProcessor.RESULT_SAVE_SUFFIX))
    }

    inner class SyncTestProcessingTest {
        private val processor = BaseTestStepProcessorForTest(testScenarioState)

        @Test
        fun testProcess() {
            val testStep = DefaultTestStep.DefaultTestStepBuilder()
                .withName("n1")
                .withId("id1")
                .build()

            val result = processor.process(testStep)
            var resultValue: Any? = null
            result.subscribe { t, r, e ->
                resultValue = r
                assertEquals(testStep, t)
                assertNull(e)
            }

            assertEquals(DEFAULT_RESULT, resultValue)
            assertEquals(testStep, testScenarioState.get(testStep.id))
            assertEquals(DEFAULT_RESULT, testScenarioState.get(testStep.getId + TestStepProcessor.RESULT_SAVE_SUFFIX))
        }

        /* @Test
         fun testProcessWithValidation() {
             val validationDetails = ValidationDetails.Builder()
                 .withTestStepId("previous test 1")
                 .withExpectedResult(ExpectedResult())
                 .build()
             val testStep = DefaultTestStep.DefaultTestStepBuilder()
                 .withName("n1")
                 .withValidationDetails(validationDetails)
                 .build()

             processor.process(testStep)

             verify(validator).validate(validationDetails.expectedResult, DEFAULT_RESULT)
         }

         @Test
         fun testProcessWithValidationWhenFails() {
             val testStep = createTestStep()

             val result = processor.process(testStep)
             var ex: RuntimeException? = null
             result.subscribe { _, _, e ->
                 ex = e
             }

             assertEquals("oops", ex!!.message)
         }

         private fun createTestStep(): DefaultTestStep {
             val validationDetails = ValidationDetails.Builder()
                 .withTestStepId("previous test 1")
                 .withExpectedResult(ExpectedResult())
                 .build()
             return DefaultTestStep.DefaultTestStepBuilder()
                 .withName("n1")
                 .withValidationDetails(validationDetails)
                 .build()
         }*/

    }

    class BaseTestStepProcessorForTest(
        testScenarioState: TestScenarioState,
    ) : BaseTestStepProcessor<DefaultTestStep>(testScenarioState) {

        override fun processTestStep(testStep: DefaultTestStep): Any = DEFAULT_RESULT

        override fun getTestStepClass(): Class<*> = DefaultTestStep::class.java
    }

    class AsyncBaseTestStepProcessorForTest(
        testScenarioState: TestScenarioState,
    ) : BaseTestStepProcessor<DefaultTestStep>(testScenarioState) {

        override fun processTestStep(testStep: DefaultTestStep): Any {
            Thread.sleep(DEFAULT_ASYNC_DELAY)
            return DEFAULT_RESULT
        }

        override fun getTestStepClass(): Class<*> = DefaultTestStep::class.java
    }
}