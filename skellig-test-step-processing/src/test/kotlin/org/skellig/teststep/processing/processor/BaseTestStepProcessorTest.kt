package org.skellig.teststep.processing.processor

import org.mockito.kotlin.mock
import org.mockito.Mockito.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.ScenarioStateUpdater
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.processing.util.ScopeLogContext
import org.skellig.teststep.processing.util.withCoroutineWorkersOfScope
import org.apache.log4j.MDC
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processing.value.function.DefaultFunctionValueExecutor
import org.skellig.teststep.processing.value.property.DefaultPropertyExtractor
import org.skellig.teststep.reader.value.expression.*
import java.math.BigDecimal

private const val DEFAULT_RESULT = "processed"
private const val DEFAULT_ASYNC_DELAY = 500L

class BaseTestStepProcessorTest {

    private val testScenarioState = DefaultTestScenarioState()
    private val scope = TestScenarioScope("test", testScenarioState)

    @Test
    fun testProcessAsync() {
        val processor = AsyncBaseTestStepProcessorForTest()
        val testStep = DefaultTestStep.DefaultTestStepBuilder().withScope(scope)
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
        val processor = AsyncBaseTestStepProcessorForTest()
        val scenarioStateUpdater = mock<ScenarioStateUpdater>()
        val testStep = DefaultTestStep.DefaultTestStepBuilder().withScope(scope)
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

    @Test
    fun `process async and close then verify it waits for the result with timeout`() {
        val processor = AsyncBaseTestStepProcessorForTest()
        val scenarioStateUpdater = mock<ScenarioStateUpdater>()
        val testStep = DefaultTestStep.DefaultTestStepBuilder().withScope(scope)
            .withName("n1")
            .withExecution(TestStepExecutionType.ASYNC)
            .withScenarioStateUpdater(listOf(scenarioStateUpdater))
            .build()

        val result = processor.process(testStep)
        var resultValue: Any? = null
        result.subscribe { _, r, _ ->
            resultValue = r
        }
        processor.close()

        verify(scenarioStateUpdater).update(resultValue, testScenarioState)
    }

    @Nested
    inner class SyncTestProcessingTest {
        private val processor = BaseTestStepProcessorForTest()
        private val valueExpressionContextFactory = ValueExpressionContextFactory(
            DefaultFunctionValueExecutor.Builder()
                .withClassLoader(BaseTestStepProcessorTest::class.java.classLoader)
                .build(),
            DefaultPropertyExtractor(null)
        )

        @Test
        fun testProcess() {
            val testStep = DefaultTestStep.DefaultTestStepBuilder().withScope(scope)
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
            val parameters = mapOf(Pair("amt", "100"), Pair(TestScenarioScope.PARAMETER_KEY, scope))
            val testStep = DefaultTestStep.DefaultTestStepBuilder().withScope(scope)
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

    @Nested
    inner class ScopeInLogContextTest {

        @Test
        fun `async test step runs with the scope id in log context although its worker was created for another scope`() {
            val scopeIds = mutableListOf<String?>()
            val processor = object : BaseTestStepProcessor<DefaultTestStep>() {
                override fun processTestStep(testStep: DefaultTestStep): Any {
                    scopeIds.add(ScopeLogContext.currentScopeId())
                    return DEFAULT_RESULT
                }

                override fun getTestStepClass(): Class<*> = DefaultTestStep::class.java
            }
            val testStep = DefaultTestStep.DefaultTestStepBuilder().withScope(TestScenarioScope("scope-1", testScenarioState))
                .withName("n1")
                .withExecution(TestStepExecutionType.ASYNC)
                .build()
            val finished = CountDownLatch(1)

            // the only worker of the processor is created by a thread of another scope and inherits its log context
            MDC.put(ScopeLogContext.KEY, "other-scope")
            try {
                processor.process(testStep).subscribe { _, _, _ -> finished.countDown() }
            } finally {
                MDC.remove(ScopeLogContext.KEY)
            }
            assertEquals(true, finished.await(10, TimeUnit.SECONDS))

            assertEquals(listOf<String?>("scope-1"), scopeIds)
        }

        @Test
        fun `tasks of test step run on shared workers with the scope id in log context`() {
            val processor = object : BaseTestStepProcessor<DefaultTestStep>() {
                override fun processTestStep(testStep: DefaultTestStep): Any =
                    runTasksAsyncAndWait(
                        mapOf<String, () -> String?>(
                            "a" to { ScopeLogContext.currentScopeId() },
                            "b" to { ScopeLogContext.currentScopeId() }
                        ), testStep
                    )

                override fun getTestStepClass(): Class<*> = DefaultTestStep::class.java
            }
            val testStep = DefaultTestStep.DefaultTestStepBuilder().withScope(TestScenarioScope("scope-1", testScenarioState))
                .withName("n1")
                .build()

            var result: Any? = null
            withCoroutineWorkersOfScope("other-scope") {
                processor.process(testStep).subscribe { _, r, _ -> result = r }
            }

            assertEquals(mapOf("a" to "scope-1", "b" to "scope-1"), result)
        }
    }

    class BaseTestStepProcessorForTest : BaseTestStepProcessor<DefaultTestStep>() {

        override fun processTestStep(testStep: DefaultTestStep): Any = DEFAULT_RESULT

        override fun getTestStepClass(): Class<*> = DefaultTestStep::class.java
    }

    class AsyncBaseTestStepProcessorForTest : BaseTestStepProcessor<DefaultTestStep>() {

        override fun processTestStep(testStep: DefaultTestStep): Any {
            Thread.sleep(DEFAULT_ASYNC_DELAY)
            return DEFAULT_RESULT
        }

        override fun getTestStepClass(): Class<*> = DefaultTestStep::class.java
    }
}