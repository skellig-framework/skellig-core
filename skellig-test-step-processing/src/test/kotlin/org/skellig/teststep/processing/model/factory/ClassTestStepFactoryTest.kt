package org.skellig.teststep.processing.model.factory

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.AnyValueExpression
import org.skellig.teststep.reader.value.expression.PatternValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression
import java.lang.reflect.Method

@DisplayName("Create ClassTestStep")
class ClassTestStepFactoryTest {

    private val factory = ClassTestStepFactory()

    @Test
    fun `when all data correct`() {
        val method = mock<Method>()
        val rawTestStep: Map<ValueExpression, ValueExpression> = mapOf(
            Pair(AlphanumericValueExpression("id"), AlphanumericValueExpression("100500")),
            Pair(AlphanumericValueExpression("testStepNamePattern"), PatternValueExpression("run (\\d+) times")),
            Pair(AlphanumericValueExpression("testStepDefClass"), AnyValueExpression(ClassTestStepFactoryTest::class.java)),
            Pair(AlphanumericValueExpression("testStepMethod"), AnyValueExpression(method)),
        )
        val testStep = factory.create("test 1", rawTestStep, mapOf(Pair("a", "1")))

        assertAll(
            { assertEquals("100500", testStep.getId) },
            { assertEquals("test 1", testStep.name) },
            { assertEquals("run (\\d+) times", testStep.testStepNamePattern.pattern()) },
            { assertEquals(ClassTestStepFactoryTest::class.java, testStep.testStepDefClass) },
            { assertEquals(method, testStep.testStepMethod) },
            { assertTrue(factory.isConstructableFrom(rawTestStep)) },
        )
    }

    @Test
    fun `when parameters have a scope Then the test step exposes it`() {
        val scope = TestScenarioScope("s1", DefaultTestScenarioState())

        val testStep = factory.create("test 1", rawTestStep(), mapOf(TestScenarioScope.PARAMETER_KEY to scope))

        assertSame(scope, testStep.scope)
    }

    @Test
    fun `when parameters have no scope Then the test step has none`() {
        assertNull(factory.create("test 1", rawTestStep(), mapOf("a" to "1")).scope)
    }

    private fun rawTestStep(): Map<ValueExpression, ValueExpression> = mapOf(
        Pair(AlphanumericValueExpression("id"), AlphanumericValueExpression("100500")),
        Pair(AlphanumericValueExpression("testStepNamePattern"), PatternValueExpression("run (\\d+) times")),
        Pair(AlphanumericValueExpression("testStepDefClass"), AnyValueExpression(ClassTestStepFactoryTest::class.java)),
        Pair(AlphanumericValueExpression("testStepMethod"), AnyValueExpression(mock<Method>())),
    )

    @Test
    fun `when test step class is null`() {
        val ex = assertThrows<IllegalStateException> {
            factory.create(
                "test 1",
                mapOf(
                    Pair(AlphanumericValueExpression("id"), AlphanumericValueExpression("100500")),
                    Pair(AlphanumericValueExpression("testStepNamePattern"), PatternValueExpression("run (\\d+) times")),
                    Pair(AlphanumericValueExpression("testStepMethod"), AnyValueExpression(mock<Method>())),
                ),
                emptyMap()
            )
        }
        assertEquals("Failed to create ClassTestStep for the test step 'test 1'. No step definition class found.", ex.message)
    }

    @Test
    fun `when test step class is not a class`() {
        val ex = assertThrows<IllegalStateException> {
            factory.create(
                "test 1",
                mapOf(
                    Pair(AlphanumericValueExpression("id"), AlphanumericValueExpression("100500")),
                    Pair(AlphanumericValueExpression("testStepNamePattern"), PatternValueExpression("run (\\d+) times")),
                    Pair(AlphanumericValueExpression("testStepDefClass"), AnyValueExpression(this)),
                    Pair(AlphanumericValueExpression("testStepMethod"), AnyValueExpression(mock<Method>())),
                ),
                emptyMap()
            )
        }
        assertEquals(
            "Failed to create ClassTestStep for the test step 'test 1'. Expected a Class instance but found '${javaClass}'", ex.message
        )
    }

    @Test
    fun `when test step method is null`() {
        val ex = assertThrows<IllegalStateException> {
            factory.create(
                "test 1",
                mapOf(
                    Pair(AlphanumericValueExpression("id"), AlphanumericValueExpression("100500")),
                    Pair(AlphanumericValueExpression("testStepNamePattern"), PatternValueExpression("run (\\d+) times")),
                    Pair(AlphanumericValueExpression("testStepDefClass"), AnyValueExpression(javaClass)),
                ),
                emptyMap()
            )
        }
        assertEquals("Failed to create ClassTestStep for the test step 'test 1'. Expected a Method instance but found 'null'", ex.message)
    }

    @Test
    fun `when test step method is invalid type`() {
        val ex = assertThrows<IllegalStateException> {
            factory.create(
                "test 1",
                mapOf(
                    Pair(AlphanumericValueExpression("id"), AlphanumericValueExpression("100500")),
                    Pair(AlphanumericValueExpression("testStepNamePattern"), PatternValueExpression("run (\\d+) times")),
                    Pair(AlphanumericValueExpression("testStepDefClass"), AnyValueExpression(javaClass)),
                    Pair(AlphanumericValueExpression("testStepMethod"), AnyValueExpression("method")),
                ),
                emptyMap()
            )
        }
        assertEquals("Failed to create ClassTestStep for the test step 'test 1'. Expected a Method instance but found 'class java.lang.String'", ex.message)
    }

    @Test
    fun `when test step name regex pattern is null`() {
        val ex = assertThrows<IllegalStateException> {
            factory.create(
                "test 1",
                mapOf(
                    Pair(AlphanumericValueExpression("id"), AlphanumericValueExpression("100500")),
                    Pair(AlphanumericValueExpression("testStepDefClass"), AnyValueExpression(javaClass)),
                    Pair(AlphanumericValueExpression("testStepMethod"), AnyValueExpression(mock<Method>())),
                ),
                emptyMap()
            )
        }
        assertEquals("Failed to create ClassTestStep for the test step 'test 1'. No regex pattern found.", ex.message)
    }
}