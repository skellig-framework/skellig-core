package org.skellig.teststep.processing.value.function

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.exception.TestDataConversionException
import org.skellig.teststep.processing.state.TestScenarioState
import java.math.BigDecimal

class GetFromStateFunctionExecutorTest {

    private var testStepStateValueConverter: GetFromStateFunctionExecutor? = null
    private var testScenarioState: TestScenarioState? = null

    @BeforeEach
    fun setUp() {
        testScenarioState = mock()
        testStepStateValueConverter = GetFromStateFunctionExecutor(testScenarioState!!)
    }

    @Test
    fun testGetSimpleValueFromStateWhenKeyNull() {
        val ex = assertThrows(TestDataConversionException::class.java) {
            testStepStateValueConverter!!.execute("get", arrayOf(null))
        }
        assertEquals("No data found in Test Scenario State with key `null`", ex.message)
    }

    @Test
    fun testGetSimpleValueFromState() {
        val expectedResult = "v1"
        whenever(testScenarioState!!.get("key")).thenReturn(expectedResult)

        assertEquals(expectedResult, testStepStateValueConverter!!.execute("get", arrayOf("key")))
    }

    @Test
    fun testGetObjectValueFromState() {
        val expectedResult = Any()
        whenever(testScenarioState!!.get("key")).thenReturn(expectedResult)

        assertEquals(expectedResult, testStepStateValueConverter!!.execute("get", arrayOf("key")))
    }

    @Test
    fun testGetValueFromStateWhenNotExist() {
        whenever(testScenarioState!!.get("key")).thenReturn(null)

        assertThrows(TestDataConversionException::class.java) { testStepStateValueConverter!!.execute("get", arrayOf("key")) }
    }

    @Test
    fun testGetValueFromStateWhenArrivesLate() {
        whenever(testScenarioState!!.get("key")).thenReturn(null)
        Thread {
            Thread.sleep(300)
            whenever(testScenarioState!!.get("key")).thenReturn("v2")
        }.start()

        assertEquals("v2", testStepStateValueConverter!!.execute("get", arrayOf("key", BigDecimal(10), BigDecimal(500))))
    }

    @Test
    fun testGetValueFromStateWhenArrivesLateAndRunOutOfAttempts() {
        whenever(testScenarioState!!.get("key")).thenReturn(null)
        Thread {
            Thread.sleep(500)
            whenever(testScenarioState!!.get("key")).thenReturn("v2")
        }.start()

        assertNull(
            testStepStateValueConverter!!.execute("get", arrayOf("key", BigDecimal(3), BigDecimal(50))),
            "No value should be found in the state"
        )
    }
}