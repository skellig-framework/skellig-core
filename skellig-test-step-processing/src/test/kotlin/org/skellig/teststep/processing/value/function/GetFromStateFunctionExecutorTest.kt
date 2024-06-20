package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.math.BigDecimal

@DisplayName("get from state")
class GetFromStateFunctionExecutorTest {

    private var getFromStateFunctionExecutor: GetFromStateFunctionExecutor? = null
    private var testScenarioState: TestScenarioState? = null

    @BeforeEach
    fun setUp() {
        testScenarioState = mock()
        getFromStateFunctionExecutor = GetFromStateFunctionExecutor(testScenarioState!!)
    }

    @Test
    fun `when key is null`() {
        val ex = assertThrows(FunctionExecutionException::class.java) {
            getFromStateFunctionExecutor!!.execute("get", null, arrayOf(null))
        }
        assertEquals("No data found in Test Scenario State with key `null`", ex.message)
    }

    @Test
    fun `when value is string`() {
        val expectedResult = "v1"
        whenever(testScenarioState!!.get("key")).thenReturn(expectedResult)

        assertEquals(expectedResult, getFromStateFunctionExecutor!!.execute("get", null, arrayOf("key")))
    }

    @Test
    fun `when value is object`() {
        val expectedResult = Any()
        whenever(testScenarioState!!.get("key")).thenReturn(expectedResult)

        assertEquals(expectedResult, getFromStateFunctionExecutor!!.execute("get", null, arrayOf("key")))
    }

    @Test
    fun `when no value for key`() {
        whenever(testScenarioState!!.get("key")).thenReturn(null)

        assertThrows<FunctionExecutionException> { getFromStateFunctionExecutor!!.execute("get", null, arrayOf("key")) }
    }

    @Test
    fun `when data doesn't exist and default provided`() {
        val defaultValue = "1000"
        whenever(testScenarioState!!.get("key")).thenReturn(null)

        assertEquals(defaultValue, getFromStateFunctionExecutor!!.execute("get", null, arrayOf("key", defaultValue)))

        verify(testScenarioState!!).set("key", defaultValue)
    }

    @Test
    fun `when no arguments provided`() {
        val ex = assertThrows<FunctionExecutionException> { getFromStateFunctionExecutor!!.execute("get", null, emptyArray()) }
        assertEquals("Function `get` can only accept 1, 2 or 3 arguments. Found 0", ex.message)
    }

    @Test
    fun `when value arrives late`() {
        whenever(testScenarioState!!.get("key")).thenReturn(null)
        Thread {
            Thread.sleep(300)
            whenever(testScenarioState!!.get("key")).thenReturn("v2")
        }.start()

        assertEquals("v2", getFromStateFunctionExecutor!!.execute("get", null, arrayOf("key", BigDecimal(10), BigDecimal(500))))
    }

    @Test
    fun `when value arrives late and runs out of attempts`() {
        whenever(testScenarioState!!.get("key")).thenReturn(null)
        Thread {
            Thread.sleep(500)
            whenever(testScenarioState!!.get("key")).thenReturn("v2")
        }.start()

        assertNull(
            getFromStateFunctionExecutor!!.execute("get", null, arrayOf("key", BigDecimal(3), BigDecimal(50))),
            "No value should be found in the state"
        )
    }
}