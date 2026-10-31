package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.math.BigDecimal

@DisplayName("get from state")
class GetFromStateFunctionExecutorTest {

    private val getFromStateFunctionExecutor = GetFromStateFunctionExecutor()
    private var testScenarioState: TestScenarioState? = null
    private var testScenarioScope : TestScenarioScope? = null

    @BeforeEach
    fun setUp() {
        testScenarioState = mock()
        testScenarioScope = TestScenarioScope("s1", testScenarioState!!)
    }

    @Test
    fun `when key is null`() {
        val ex = assertThrows(FunctionExecutionException::class.java) {
            getFromStateFunctionExecutor.execute("get", null, arrayOf(null), testScenarioScope)
        }
        assertEquals("No data found in Test Scenario State with key `null`", ex.message)
    }

    @Test
    fun `when value is string`() {
        val expectedResult = "v1"
        whenever(testScenarioState!!.get("key")).thenReturn(expectedResult)

        assertEquals(expectedResult, getFromStateFunctionExecutor.execute("get", null, arrayOf("key"), testScenarioScope))
    }

    @Test
    fun `when value is object`() {
        val expectedResult = Any()
        whenever(testScenarioState!!.get("key")).thenReturn(expectedResult)

        assertEquals(expectedResult, getFromStateFunctionExecutor.execute("get", null, arrayOf("key"), testScenarioScope))
    }

    @Test
    fun `when no value for key`() {
        whenever(testScenarioState!!.get("key")).thenReturn(null)

        assertThrows<FunctionExecutionException> { getFromStateFunctionExecutor.execute("get", null, arrayOf("key"), testScenarioScope) }
    }

    @Test
    fun `when data doesn't exist and default provided`() {
        val defaultValue = "1000"
        whenever(testScenarioState!!.get("key")).thenReturn(null)

        assertEquals(defaultValue, getFromStateFunctionExecutor.execute("get", null, arrayOf("key", defaultValue), testScenarioScope))

        verify(testScenarioState!!).set("key", defaultValue)
    }

    @Test
    fun `when no arguments provided`() {
        val ex = assertThrows<FunctionExecutionException> { getFromStateFunctionExecutor.execute("get", null, emptyArray(), testScenarioScope) }
        assertEquals("Function `get` can only accept 1, 2 or 3 arguments. Found 0", ex.message)
    }

    @Test
    fun `when value arrives late`() {
        whenever(testScenarioState!!.get("key")).thenReturn(null)
        Thread {
            Thread.sleep(300)
            whenever(testScenarioState!!.get("key")).thenReturn("v2")
        }.start()

        assertEquals("v2", getFromStateFunctionExecutor.execute("get", null, arrayOf("key", BigDecimal(10), BigDecimal(500)), testScenarioScope))
    }

    @Test
    fun `when value arrives late and runs out of attempts`() {
        whenever(testScenarioState!!.get("key")).thenReturn(null)
        Thread {
            Thread.sleep(500)
            whenever(testScenarioState!!.get("key")).thenReturn("v2")
        }.start()

        assertNull(
            getFromStateFunctionExecutor.execute("get", null, arrayOf("key", BigDecimal(3), BigDecimal(50)), testScenarioScope),
            "No value should be found in the state"
        )
    }

    @Test
    fun `when scope is provided then sets the default value in the state of the scope`() {
        val scopeState = DefaultTestScenarioState()

        getFromStateFunctionExecutor.execute("get", null, arrayOf("key", "v1"), TestScenarioScope("s1", scopeState))

        assertEquals("v1", scopeState.get("key"))
    }

    @Test
    fun `when scope is null then fails`() {
        val ex = assertThrows(FunctionExecutionException::class.java) {
            getFromStateFunctionExecutor.execute("get", null, arrayOf("key"), null)
        }
        assertEquals("Function `get` requires a Test Scenario Scope, but the step has none", ex.message)
    }

    @Test
    fun `executor which is not aware of scope is executed without it`() {
        val legacy = object : FunctionValueExecutor {
            override fun execute(name: String, value: Any?, args: Array<Any?>): Any? = "legacy ${args[0]}"
            override fun getFunctionName(): String = "legacy"
        }

        assertEquals("legacy a", legacy.execute("legacy", null, arrayOf("a"), TestScenarioScope("s1", DefaultTestScenarioState())))
    }
}
