package org.skellig.teststep.processing.converter

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.exception.TestDataConversionException
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor

class TestStepStateValueConverterTest {

    private var testStepStateValueConverter: TestStepStateValueConverter? = null
    private var testScenarioState: TestScenarioState? = null

    @BeforeEach
    fun setUp() {
        testScenarioState = mock()
        testStepStateValueConverter = TestStepStateValueConverter(testScenarioState!!)
    }

    @Test
    fun testGetSimpleValueFromStateWhenKeyNull() {
        val ex = Assertions.assertThrows(TestDataConversionException::class.java) {
            testStepStateValueConverter!!.execute("get", arrayOf(null))
        }
        Assertions.assertEquals("No data found in Test Scenario State with key `null`", ex.message)
    }

    @Test
    fun testGetSimpleValueFromState() {
        val expectedResult = "v1"
        whenever(testScenarioState!!.get("key")).thenReturn(expectedResult)

        Assertions.assertEquals(expectedResult, testStepStateValueConverter!!.execute("get", arrayOf("key")))
    }

    @Test
    fun testGetObjectValueFromState() {
        val expectedResult = Any()
        whenever(testScenarioState!!.get("key")).thenReturn(expectedResult)

        Assertions.assertEquals(expectedResult, testStepStateValueConverter!!.execute("get", arrayOf("key")))
    }

    @Test
    fun testGetValueFromStateWhenNotExist() {
        whenever(testScenarioState!!.get("key")).thenReturn(null)

        Assertions.assertThrows(TestDataConversionException::class.java) { testStepStateValueConverter!!.execute("get", arrayOf("key")) }
    }
}