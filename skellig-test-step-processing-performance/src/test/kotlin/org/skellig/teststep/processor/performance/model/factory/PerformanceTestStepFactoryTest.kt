package org.skellig.teststep.processor.performance.model.factory

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.skellig.teststep.processing.exception.TestStepCreationException
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.TestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processing.value.function.DefaultFunctionValueExecutor
import org.skellig.teststep.processing.value.property.DefaultPropertyExtractor
import org.skellig.teststep.reader.value.expression.ValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.alphaNum
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.list
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.map
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.num
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.string
import java.math.BigDecimal
import java.time.LocalTime
import java.time.format.DateTimeParseException

class PerformanceTestStepFactoryTest {

    private val testStepFactory = mock<TestStepFactory<TestStep>>()
    private val factory = PerformanceTestStepFactory(
        testStepFactory,
        ValueExpressionContextFactory(
            DefaultFunctionValueExecutor.Builder()
                .withTestScenarioState(mock())
                .withClassLoader(this.javaClass.classLoader)
                .build(), DefaultPropertyExtractor(null)
        )
    )

    @Test
    fun `verify if test step is constructable`() {
        assertTrue(
            factory.isConstructableFrom(
                mapOf(
                    Pair(alphaNum("rps"), num("1")),
                    Pair(alphaNum("timeToRun"), string("00:00:14")),
                    Pair(alphaNum("run"), list(string("run test 1"))),
                )
            ), "Test step must be constructable"
        )
        assertFalse(
            factory.isConstructableFrom(
                mapOf(
                    Pair(alphaNum("timeToRun"), string("00:00:14")),
                    Pair(alphaNum("run"), list(string("run test 1"))),
                )
            ), "Test step must not be constructable"
        )
        assertFalse(
            factory.isConstructableFrom(
                mapOf(
                    Pair(alphaNum("rps"), num("1")),
                    Pair(alphaNum("run"), list(string("run test 1"))),
                )
            ), "Test step must not be constructable"
        )
        assertFalse(
            factory.isConstructableFrom(
                mapOf(
                    Pair(alphaNum("rps"), num("1")),
                    Pair(alphaNum("timeToRun"), string("00:00:14")),
                )
            ), "Test step must not be constructable"
        )
    }

    @Test
    fun `create test step`() {
        val parameters = mapOf(Pair("a", "b"))
        val testStep = factory.create(
            "t1",
            mapOf(
                Pair(alphaNum("rps"), num("120")),
                Pair(alphaNum("timeToRun"), string("01:10:30")),
                Pair(alphaNum("before"), list(string("test A"))),
                Pair(alphaNum("after"), list(string("test B"))),
                Pair(alphaNum("run"), list(string("run test 1"), string("run test 2"))),
            ),
            parameters
        )
        val rawTestStepBefore = mock<Map<ValueExpression, ValueExpression?>>()
        val rawTestStepAfter = mock<Map<ValueExpression, ValueExpression?>>()
        val rawTestStepRun1 = mock<Map<ValueExpression, ValueExpression?>>()
        val rawTestStepRun2 = mock<Map<ValueExpression, ValueExpression?>>()

        val testStepRegistry = mock<TestStepRegistry>()
        whenever(testStepRegistry.getByName("test A")).thenReturn(rawTestStepBefore)
        whenever(testStepRegistry.getByName("test B")).thenReturn(rawTestStepAfter)
        whenever(testStepRegistry.getByName("run test 1")).thenReturn(rawTestStepRun1)
        whenever(testStepRegistry.getByName("run test 2")).thenReturn(rawTestStepRun2)

        testStep.testStepsToRunBefore.forEach { it.invoke(testStepRegistry) }
        testStep.testStepsToRunAfter.forEach { it.invoke(testStepRegistry) }
        testStep.testStepsToRun.forEach { it.invoke(testStepRegistry) }

        assertAll(
            { assertEquals("t1", testStep.name) },
            { assertEquals(120, testStep.rps) },
            { assertEquals(LocalTime.of(1, 10, 30), testStep.timeToRun) },
            { verify(testStepFactory).create("test A", rawTestStepBefore, parameters) },
            { verify(testStepFactory).create("test B", rawTestStepAfter, parameters) },
            { verify(testStepFactory).create("run test 1", rawTestStepRun1, parameters) },
            { verify(testStepFactory).create("run test 2", rawTestStepRun2, parameters) },
        )
    }

    @Test
    fun `create test step without before and after`() {
        val testStep = factory.create(
            "t1",
            mapOf(
                Pair(alphaNum("rps"), num("1")),
                Pair(alphaNum("timeToRun"), string("00:00:14")),
                Pair(
                    alphaNum("run"), list(
                        map(
                            Pair(alphaNum("name"), string("run test 1")),
                            Pair(alphaNum("parameters"), map(Pair(alphaNum("a"), num("1"))))
                        )
                    )
                ),
            ),
            mapOf(Pair("a", "100"), Pair("b", "10"))
        )
        val rawTestStepRun1 = mock<Map<ValueExpression, ValueExpression?>>()
        val testStepRegistry = mock<TestStepRegistry>()
        whenever(testStepRegistry.getByName("run test 1")).thenReturn(rawTestStepRun1)

        testStep.testStepsToRun.forEach { it.invoke(testStepRegistry) }

        assertAll(
            { assertTrue(testStep.testStepsToRunBefore.isEmpty(), "No before steps must be added") },
            { assertTrue(testStep.testStepsToRunAfter.isEmpty(), "No after steps must be added") },
            // verify that inner parameter 'a' replaces the one from parent
            { verify(testStepFactory).create("run test 1", rawTestStepRun1, mapOf(Pair("a", BigDecimal("1")), Pair("b", "10"))) },
        )
    }

    @Test
    fun `create test step with run property is invalid type`() {
        val ex = assertThrows<TestStepCreationException> {factory.create(
            "t1",
            mapOf(
                Pair(alphaNum("rps"), num("1")),
                Pair(alphaNum("timeToRun"), string("00:00:14")),
                Pair(
                    alphaNum("run"), list(num("1"))
                ),
            ),
            mapOf(Pair("a", "100"), Pair("b", "10"))
        )}

        assertEquals(  "Invalid data type of 'run' in test step 't1'. " +
                "Must have a list of test steps to run with parameters or without", ex.message)
    }

    @Test
    fun `create test step when test step to run not found`() {
        val testStep = factory.create(
            "t1",
            mapOf(
                Pair(alphaNum("rps"), num("1")),
                Pair(alphaNum("timeToRun"), string("00:00:14")),
                Pair(alphaNum("run"), list(string("run test 1"))),
            ),
            emptyMap()
        )
        val testStepRegistry = mock<TestStepRegistry>()
        whenever(testStepRegistry.getByName("run test 1")).thenReturn(null)

        val ex = assertThrows<IllegalStateException> { testStep.testStepsToRun.forEach { it.invoke(testStepRegistry) } }

        assertEquals("Test step 'run test 1' is not found in any of test data files or classes indicated in the runner", ex.message)
    }

    @Test
    fun `create test step with invalid rps type`() {
        val ex = assertThrows<TestStepCreationException> {
            factory.create(
                "t1",
                mapOf(
                    Pair(alphaNum("rps"), string("10")),
                    Pair(alphaNum("timeToRun"), string("00:00:14")),
                    Pair(alphaNum("run"), list(string("run test 1"))),
                ),
                emptyMap()
            )
        }
        assertEquals("Failed to parse 'RPS' property", ex.message)
        assertEquals(ClassCastException::class.java, ex.cause?.javaClass)

        val ex2 = assertThrows<TestStepCreationException> {
            factory.create(
                "t1",
                mapOf(
                    Pair(alphaNum("timeToRun"), string("00:00:14")),
                    Pair(alphaNum("run"), list(string("run test 1"))),
                ),
                emptyMap()
            )
        }
        assertEquals("The value of 'RPS' property must not be null", ex2.cause?.message)
    }

    @Test
    fun `create test step with invalid timeToRun format`() {
        val ex = assertThrows<TestStepCreationException> {
            factory.create(
                "t1",
                mapOf(
                    Pair(alphaNum("rps"), num("10")),
                    Pair(alphaNum("timeToRun"), string("10")),
                    Pair(alphaNum("run"), list(string("run test 1"))),
                ),
                emptyMap()
            )
        }
        assertEquals("Failed to parse 'timeToRun' property", ex.message)
        assertEquals(DateTimeParseException::class.java, ex.cause?.javaClass)

        val ex2 = assertThrows<TestStepCreationException> {
            factory.create(
                "t1",
                mapOf(
                    Pair(alphaNum("rps"), num("10")),
                    Pair(alphaNum("run"), list(string("run test 1"))),
                ),
                emptyMap()
            )
        }
        assertEquals("The value of 'timeToRun' property must not be null", ex2.cause?.message)
    }
}