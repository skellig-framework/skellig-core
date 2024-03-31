package org.skellig.teststep.processing.processor.task

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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

    @Nested
    inner class ForEachTaskProcessorTest {

        @Test
        fun testForEachWithVariablesAndRunningTestStep() {
            val taskProcessor = createTaskProcessor { testName, parameters ->
                testScenarioState.set(testName, "passed")
                testScenarioState.set("parameters", parameters)

                TestStepProcessor.TestStepRunResult(mock<TestStep>())
            }

            val value = MapValueExpression(
                mapOf(
                    Pair(
                        AlphanumericValueExpression("a"), MapValueExpression(
                            mapOf(
                                Pair(AlphanumericValueExpression("p1"), PropertyValueExpression("i"))
                            )
                        )
                    ),
                    Pair(AlphanumericValueExpression("b"), CallChainExpression(listOf(PropertyValueExpression("a"), AlphanumericValueExpression("p1")))),
                    Pair(
                        FunctionCallExpression("runTest", arrayOf(AlphanumericValueExpression("test A"))),
                        MapValueExpression(
                            mapOf(
                                Pair(
                                    AlphanumericValueExpression("parameters"),
                                    MapValueExpression(mapOf(Pair(AlphanumericValueExpression("number"), PropertyValueExpression("i"))))
                                )
                            )
                        )
                    )
                )
            )
            val parameters = mutableMapOf<String, Any?>(Pair("items", listOf("1", "2")))
            taskProcessor.process(
                FunctionCallExpression(
                    "forEach",
                    arrayOf(PropertyValueExpression("items"), AlphanumericValueExpression("i"))
                ), value, parameters
            )

            assertAll(
                { assertEquals(mapOf(Pair("p1", "2")), parameters["a"]) },
                { assertEquals("2", parameters["b"]) },
                { assertEquals("2", parameters["i"]) },
                { assertEquals("passed", testScenarioState.get("test A")) },
                { assertEquals(mapOf(Pair("number", "2")), testScenarioState.get("parameters")) }
            )
        }

        @Test
        fun testAsyncForEach() {
            val taskProcessor = createTaskProcessor { testName, _ ->
                testScenarioState.set(testName, "passed")

                val testStepRunResult = TestStepProcessor.TestStepRunResult(mock<TestStep>())
                testStepRunResult.notify("response", null)
                testStepRunResult
            }

            val value = MapValueExpression(
                mapOf(
                    Pair(
                        MathOperationExpression("+", AlphanumericValueExpression("p."), CallChainExpression(listOf(PropertyValueExpression("i"), AlphanumericValueExpression("num")))),
                        CallChainExpression(listOf(PropertyValueExpression("i"), AlphanumericValueExpression("code")))
                    ),
                    Pair(
                        FunctionCallExpression("runTest", arrayOf(AlphanumericValueExpression("test A"))),
                        MapValueExpression(
                            mapOf(
                                Pair(
                                    AlphanumericValueExpression("onPassed"),
                                    MapValueExpression(
                                        mapOf(
                                            Pair(
                                                AlphanumericValueExpression("state"),
                                                MapValueExpression(
                                                    mapOf(
                                                        Pair(
                                                            MathOperationExpression(
                                                                "+",
                                                                AlphanumericValueExpression("stored-p."),
                                                                CallChainExpression(listOf(PropertyValueExpression("i"), AlphanumericValueExpression("num")))
                                                            ),
                                                            PropertyValueExpression(
                                                                MathOperationExpression(
                                                                    "+",
                                                                    AlphanumericValueExpression("p."),
                                                                    CallChainExpression(listOf(PropertyValueExpression("i"), AlphanumericValueExpression("num")))
                                                                )
                                                            )
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )

                    )
                )
            )
            val parameters = mutableMapOf<String, Any?>(
                Pair(
                    "items",
                    listOf(
                        mapOf(Pair("num", "1"), Pair("code", "1234")),
                        mapOf(Pair("num", "2"), Pair("code", "4321"))
                    )
                )
            )
            taskProcessor.process(
                FunctionCallExpression(
                    "asyncForEach",
                    arrayOf(PropertyValueExpression("items"), AlphanumericValueExpression("i"))
                ), value, parameters
            )

            assertAll(
                { assertEquals("1234", testScenarioState.get("stored-p.1")) },
                { assertEquals("4321", testScenarioState.get("stored-p.2")) }
            )
        }

        @Test
        fun testForEachWithInvalidType() {
            val taskProcessor = createTaskProcessor()

            val ex = assertThrows<IllegalStateException> {
                taskProcessor.process(
                    FunctionCallExpression(
                        "forEach",
                        arrayOf(PropertyValueExpression("items"), AlphanumericValueExpression("i"))
                    ), ListValueExpression(listOf()), mutableMapOf()
                )
            }

            assertEquals(
                "Invalid property type of the function 'forEach'. Expected key-value pairs, found ${ListValueExpression::class.java}", ex.message
            )
        }
    }

    @Nested
    inner class RunIfTaskProcessorTest {

        @Test
        fun testRunIfTrueOrFalse() {
            val taskProcessor = createTaskProcessor()

            val value = MapValueExpression(
                mapOf(
                    Pair(AlphanumericValueExpression("b"), ListValueExpression(listOf(AlphanumericValueExpression("v2")))),
                )
            )
            val parameters = mutableMapOf<String, Any?>(Pair("result", "success"))
            taskProcessor.process(
                FunctionCallExpression(
                    "runIf",
                    arrayOf(ValueComparisonExpression("==", PropertyValueExpression("result"), AlphanumericValueExpression("success")))
                ), value, parameters
            )

            assertEquals(listOf("v2"), parameters["b"])

            parameters.remove("b")
            taskProcessor.process(
                FunctionCallExpression(
                    "runIf",
                    arrayOf(ValueComparisonExpression("==", PropertyValueExpression("result"), AlphanumericValueExpression("failed")))
                ), value, parameters
            )

            assertFalse(parameters.contains("b"))
        }

        @Test
        fun testRunIfWhenInvalidType() {
            val taskProcessor = createTaskProcessor()

            val value = ListValueExpression(listOf(AlphanumericValueExpression("b")))
            val parameters = mutableMapOf<String, Any?>()

            val ex = assertThrows<IllegalStateException> {
                taskProcessor.process(
                    FunctionCallExpression(
                        "runIf",
                        arrayOf(ValueComparisonExpression("==", PropertyValueExpression("result"), AlphanumericValueExpression("success")))
                    ), value, parameters
                )
            }

            assertEquals(
                "Invalid property type of the function 'runIf'. " +
                        "Expected key-value pairs, found ${ListValueExpression::class.java}", ex.message
            )
        }

        @Test
        fun testRunIfWhenNoComparison() {
            val taskProcessor = createTaskProcessor()

            val value = MapValueExpression(
                mapOf(
                    Pair(AlphanumericValueExpression("b"), ListValueExpression(listOf(AlphanumericValueExpression("v2")))),
                )
            )
            val parameters = mutableMapOf<String, Any?>()

            taskProcessor.process(FunctionCallExpression("runIf", emptyArray()), value, parameters)

            assertFalse(parameters.contains("b"))
        }

    }

    @Nested
    inner class StateTaskProcessorTest {

        @Test
        fun testSaveToState() {
            val taskProcessor = createTaskProcessor()

            val value = MapValueExpression(
                mapOf(
                    Pair(AlphanumericValueExpression("a"), ListValueExpression(listOf(PropertyValueExpression("result")))),
                )
            )
            val parameters = mutableMapOf<String, Any?>(Pair("result", "success"))
            taskProcessor.process(AlphanumericValueExpression("state"), value, parameters)

            assertEquals(listOf("success"), testScenarioState.get("a"))
        }

        @Test
        fun testSaveToStateWithNullKey() {
            val taskProcessor = createTaskProcessor()

            val value = MapValueExpression(
                mapOf(
                    Pair(PropertyValueExpression("a"), ListValueExpression(listOf(PropertyValueExpression("result")))),
                )
            )
            val parameters = mutableMapOf<String, Any?>()
            val ex = assertThrows<IllegalStateException> {
                taskProcessor.process(AlphanumericValueExpression("state"), value, parameters)
            }

            assertEquals("Cannot set value to the null key in the Test Scenario State", ex.message)
        }

        @Test
        fun testStateWhenInvalidType() {
            val taskProcessor = createTaskProcessor()

            val value = ListValueExpression(listOf(AlphanumericValueExpression("a")))

            val ex = assertThrows<IllegalStateException> {
                taskProcessor.process(AlphanumericValueExpression("state"), value, mutableMapOf())
            }

            assertEquals(
                "Invalid property type of the function 'state'. Expected key-value pairs, found ${ListValueExpression::class.java}", ex.message
            )
        }

    }

    @Nested
    inner class RunTaskProcessorTest {

        @Test
        fun testRunTaskWithCallbacks() {
            val taskProcessor = createTaskProcessor { _, _ ->
                val testStepRunResult = TestStepProcessor.TestStepRunResult(mock<TestStep>())
                testStepRunResult.notify(null, RuntimeException("test failed"))
                testStepRunResult
            }

            val value = MapValueExpression(
                mapOf(
                    Pair(
                        AlphanumericValueExpression("onFailed"),
                        MapValueExpression(mapOf(Pair(AlphanumericValueExpression("a"), NumberValueExpression("100"))))
                    )
                )
            )
            val parameters = mutableMapOf<String, Any?>()
            taskProcessor.process(FunctionCallExpression("runTest", arrayOf(AlphanumericValueExpression("test A"))), value, parameters)

            assertEquals(BigDecimal("100"), parameters["a"])
        }

        @Test
        fun testRunTaskWhenInvalidType() {
            val taskProcessor = createTaskProcessor()

            val value = ListValueExpression(emptyList())

            val ex = assertThrows<IllegalStateException> {
                taskProcessor.process(FunctionCallExpression("runTest", arrayOf(AlphanumericValueExpression("test A"))), value, mutableMapOf())
            }

            assertEquals(
                "Invalid property type of the function 'runTest'. Expected key-value pairs, found ${ListValueExpression::class.java}", ex.message
            )
        }

        @Test
        fun testRunTaskWhenParametersNotMapOrEmpty() {
            val taskProcessor = createTaskProcessor { _, parameters ->
                testScenarioState.set("parameters", parameters)
                val testStepRunResult = TestStepProcessor.TestStepRunResult(mock<TestStep>())
                testStepRunResult.notify("response", null)
                testStepRunResult
            }

            val value = MapValueExpression(
                mapOf(
                    Pair(
                        AlphanumericValueExpression("parameters"),
                        ListValueExpression(listOf(AlphanumericValueExpression("a")))
                    )
                )
            )
            val parameters = mutableMapOf<String, Any?>(Pair("p1", "v1"))
            taskProcessor.process(FunctionCallExpression("runTest", arrayOf(AlphanumericValueExpression("test A"))), value, parameters)

            assertEquals(parameters, testScenarioState.get("parameters"))

            taskProcessor.process(
                FunctionCallExpression("runTest", arrayOf(AlphanumericValueExpression("test A"))),
                MapValueExpression(mapOf()), parameters
            )

            assertEquals(parameters, testScenarioState.get("parameters"))
        }

        @Test
        fun testRunTaskWhenTestStepNameEvaluatedToNull() {
            val taskProcessor = createTaskProcessor()

            val ex = assertThrows<IllegalStateException> {
                taskProcessor.process(
                    FunctionCallExpression("runTest", arrayOf(PropertyValueExpression("test_A"))),
                    MapValueExpression(mapOf()), mutableMapOf()
                )
            }

            assertEquals("The Test Step '\${test_A}' was evaluated to 'null'", ex.message)
        }

    }

    @Test
    fun testGetTaskName() {
        assertEquals("", createTaskProcessor().getTaskName())
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
