package org.skellig.teststep.processing.processor

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.ScenarioStateUpdater
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processing.value.function.DefaultFunctionValueExecutor
import org.skellig.teststep.processing.value.property.DefaultPropertyExtractor
import org.skellig.teststep.reader.value.expression.*
import java.math.BigDecimal

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

    @Test
    fun testProcessAsyncWithStateUpdate() {
        val processor = AsyncBaseTestStepProcessorForTest(testScenarioState)
        val scenarioStateUpdater = mock<ScenarioStateUpdater>()
        val testStep = DefaultTestStep.DefaultTestStepBuilder()
            .withName("n1")
            .withExecution(TestStepExecutionType.ASYNC)
            .withScenarioStateUpdater(listOf(scenarioStateUpdater))
            .build()

        val result = processor.process(testStep)
        var resultValue: Any? = null
        result.subscribe { _, r, _ ->
            resultValue = r
        }

        Thread.sleep(DEFAULT_ASYNC_DELAY + 100L)
        verify(scenarioStateUpdater).update(resultValue, testScenarioState)
    }

    @Nested
    inner class SyncTestProcessingTest {
        private val processor = BaseTestStepProcessorForTest(testScenarioState)
        private val valueExpressionContextFactory = ValueExpressionContextFactory(
            DefaultFunctionValueExecutor.Builder()
                .withTestScenarioState(testScenarioState)
                .withClassLoader(BaseTestStepProcessorTest::class.java.classLoader)
                .build(),
            DefaultPropertyExtractor(null)
        )

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

        @Test
        fun testProcessWithStateUpdate() {
            val parameters = mapOf(Pair("amt", "100"))
            val testStep = DefaultTestStep.DefaultTestStepBuilder()
                .withName("n1")
                .withScenarioStateUpdater(
                    listOf(
                        ScenarioStateUpdater(
                            AlphanumericValueExpression("a"),
                            CallChainExpression(
                                listOf(
                                    PropertyValueExpression("amt", null),
                                    FunctionCallExpression("toBigDecimal", emptyArray())
                                )
                            ),
                            parameters,
                            valueExpressionContextFactory
                        ),
                        ScenarioStateUpdater(
                            AlphanumericValueExpression("b"),
                            MathOperationExpression("+", AlphanumericValueExpression("\$result"), FunctionCallExpression("get", arrayOf(AlphanumericValueExpression("a")))),
                            parameters,
                            valueExpressionContextFactory
                        )
                    )
                )
                .build()

            processor.process(testStep)

            assertEquals(BigDecimal("100"), testScenarioState.get("a"))
            assertEquals("${DEFAULT_RESULT}100", testScenarioState.get("b"))
        }
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