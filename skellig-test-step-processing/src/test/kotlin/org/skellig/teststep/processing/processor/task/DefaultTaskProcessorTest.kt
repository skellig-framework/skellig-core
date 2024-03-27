package org.skellig.teststep.processing.processor.task

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processing.value.ValueExpressionContextFactoryTest
import org.skellig.teststep.processing.value.function.DefaultFunctionValueExecutor
import org.skellig.teststep.processing.value.property.DefaultPropertyExtractor
import org.skellig.teststep.reader.value.expression.*
import java.math.BigDecimal

class DefaultTaskProcessorTest {

    private val testStepProcessor = mock<TestStepProcessor<*>>()
    private val testScenarioState = DefaultTestScenarioState()
    private val valueExpressionContextFactory = ValueExpressionContextFactory(
        DefaultFunctionValueExecutor.Builder()
            .withTestScenarioState(testScenarioState)
            .withClassLoader(ValueExpressionContextFactoryTest::class.java.classLoader)
            .build(),
        DefaultPropertyExtractor(null)
    )

    @Nested
    inner class VariableTaskProcessorTest {

        @Test
        fun testSetVariables() {
            testScenarioState.set("delta", "0.1")
            val taskProcessor = createTaskProcessor()

            val value = MapValueExpression(
                mapOf(
                    Pair(AlphanumericValueExpression("a"), NumberValueExpression("100")),
                    Pair(AlphanumericValueExpression("b"), MathOperationExpression("/", PropertyValueExpression("a"), NumberValueExpression("2"))),
                    Pair(AlphanumericValueExpression("c"), MathOperationExpression("*", FunctionCallExpression("get", arrayOf(AlphanumericValueExpression("delta"))), PropertyValueExpression("b"))),
                )
            )
            val parameters = mutableMapOf<String, Any?>()
            taskProcessor.process(null, value, parameters)

            assertAll(
                { assertEquals(BigDecimal(100), parameters["a"]) },
                { assertEquals(BigDecimal(50), parameters["b"]) },
                { assertEquals(BigDecimal("5.0"), parameters["c"]) },
            )
        }

        @Test
        fun testSetMapAndListVariables() {
            testScenarioState.set("p1", "v1")
            val taskProcessor = createTaskProcessor()

            val value = MapValueExpression(
                mapOf(
                    Pair(
                        AlphanumericValueExpression("a"), MapValueExpression(
                            mapOf(
                                Pair(AlphanumericValueExpression("p1"), FunctionCallExpression("get", arrayOf(AlphanumericValueExpression("p1"))))
                            )
                        )
                    ),
                    Pair(AlphanumericValueExpression("b"), ListValueExpression(listOf(AlphanumericValueExpression("v2")))),
                )
            )
            val parameters = mutableMapOf<String, Any?>()
            taskProcessor.process(null, value, parameters)

            assertAll(
                { assertEquals(mapOf(Pair("p1", "v1")), parameters["a"]) },
                { assertEquals(listOf("v2"), parameters["b"]) },
            )
        }

        @Test
        fun testSetVariablesWhereKeyIsNull() {
            val taskProcessor = createTaskProcessor()
            val value = NumberValueExpression("100")

            val ex = assertThrows<IllegalStateException> {
                taskProcessor.process(PropertyValueExpression("a"), value, mutableMapOf())
            }

            assertEquals("Cannot assign value to the null key", ex.message)

            val ex2 = assertThrows<IllegalStateException> {
                taskProcessor.process(null, value, mutableMapOf())
            }
            assertEquals("Key must not be null or Value type must be MapValueExpression", ex2.message)
        }
    }

    @Nested
    inner class AsyncEachTaskProcessorTest {

        @Test
        fun testSetVariablesInAsyncEach() {
            testScenarioState.set("p1", "v1")
            val taskProcessor = createTaskProcessor()

            val value = MapValueExpression(
                mapOf(
                    Pair(
                        AlphanumericValueExpression("a"), MapValueExpression(
                            mapOf(
                                Pair(AlphanumericValueExpression("p1"), FunctionCallExpression("get", arrayOf(AlphanumericValueExpression("p1"))))
                            )
                        )
                    ),
                    Pair(AlphanumericValueExpression("b"), ListValueExpression(listOf(AlphanumericValueExpression("v2")))),
                )
            )
            val parameters = mutableMapOf<String, Any?>()
            taskProcessor.process(AlphanumericValueExpression("asyncEach"), value, parameters)

            assertAll(
                { assertEquals(mapOf(Pair("p1", "v1")), parameters["a"]) },
                { assertEquals(listOf("v2"), parameters["b"]) },
            )
        }

        @Test
        fun testAsyncEachWithInvalidTypeValue() {
            val taskProcessor = createTaskProcessor()

            val value = ListValueExpression(
                listOf(
                    AlphanumericValueExpression("first"),
                    AlphanumericValueExpression("second"),
                )
            )
            val ex = assertThrows<IllegalStateException> {
                taskProcessor.process(AlphanumericValueExpression("asyncEach"), value, mutableMapOf())
            }

            assertEquals("Invalid property type of the function 'asyncEach'. Expected key-value pairs, found ${ListValueExpression::class.java}", ex.message)
        }

        @Test
        fun testRunTestsInAsyncEachWithDifferentCompletionTime() {
            val taskProcessor = createTaskProcessor { testName, _ ->
                when (testName) {
                    "test A" -> Thread.sleep(500)
                    "test C" -> Thread.sleep(900)
                    else -> Thread.sleep(100)
                }
                testScenarioState.set(testName, "passed")
                val testStepRunResult = TestStepProcessor.TestStepRunResult(mock<TestStep>())
                testStepRunResult.notify("response", null)
                testStepRunResult
            }

            val value = MapValueExpression(
                mapOf(
                    Pair(FunctionCallExpression("runTest", arrayOf(AlphanumericValueExpression("test A"))), MapValueExpression(mapOf())),
                    Pair(
                        FunctionCallExpression("runTest", arrayOf(AlphanumericValueExpression("test B"))),
                        MapValueExpression(
                            mapOf(
                                Pair(
                                    AlphanumericValueExpression("onPassed"),
                                    MapValueExpression(mapOf(Pair(FunctionCallExpression("runTest", arrayOf(AlphanumericValueExpression("test C"))), MapValueExpression(mapOf()))))
                                )
                            )
                        )
                    ),
                )
            )
            val parameters = mutableMapOf<String, Any?>()
            taskProcessor.process(AlphanumericValueExpression("asyncEach"), value, parameters)

            assertAll(
                { assertEquals("passed", testScenarioState.get("test A")) },
                { assertEquals("passed", testScenarioState.get("test B")) },
                { assertEquals("passed", testScenarioState.get("test C")) },
            )
        }
    }

    private fun createTaskProcessor(runTestDelegate: (String, Map<String, Any?>?) -> TestStepProcessor.TestStepRunResult): DefaultTaskProcessor {
        return DefaultTaskProcessor(
            testScenarioState,
            { value, parameters -> value?.evaluate(valueExpressionContextFactory.create(parameters)) },
            runTestDelegate
        )
    }

    private fun createTaskProcessor(): DefaultTaskProcessor {
        return createTaskProcessor { _, _ -> TestStepProcessor.TestStepRunResult(mock<TestStep>()) }
    }
}
