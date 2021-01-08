package org.skellig.teststep.processing.converter

import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.skellig.teststep.processing.exception.TestDataConversionException
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor

class TestStepStateValueConverterTest {

    private var testStepStateValueConverter: TestStepStateValueConverter? = null
    private var testScenarioState: TestScenarioState? = null
    private var valueExtractor: TestStepValueExtractor? = null

    @BeforeEach
    fun setUp() {
        testScenarioState = Mockito.mock(TestScenarioState::class.java)
        valueExtractor = Mockito.mock(TestStepValueExtractor::class.java)
        testStepStateValueConverter = TestStepStateValueConverter(testScenarioState!!, valueExtractor)
    }

    @Test
    fun testGetSimpleValueFromState() {
        val expectedResult = "v1"
        whenever(testScenarioState!!.get("key")).thenReturn(expectedResult)

        Assertions.assertEquals(expectedResult, testStepStateValueConverter!!.convert("get(key)"))
    }

    @Test
    fun testGetObjectValueFromState() {
        val expectedResult = Any()
        whenever(testScenarioState!!.get("key")).thenReturn(expectedResult)

        Assertions.assertEquals(expectedResult, testStepStateValueConverter!!.convert("get(key)"))
    }

    @Test
    fun testGetValueFromStateWithAttachedString() {
        whenever(testScenarioState!!.get("key")).thenReturn(listOf("_"))

        Assertions.assertEquals("^[_]^", testStepStateValueConverter!!.convert("^get(key)^"))
    }

    @Test
    fun testGetValueFromStateWithExtractorAndAttachedString() {
        val value = listOf("_")
        whenever(valueExtractor!!.extract(value, "([0])")).thenReturn(value[0])
        whenever(testScenarioState!!.get("key")).thenReturn(value)

        Assertions.assertEquals("^_^", testStepStateValueConverter!!.convert("^get(key).([0])^"))
    }

    @Test
    fun testGetValueFromStateWithExtractor() {
        val value = "value"
        whenever(valueExtractor!!.extract(value, "(length)")).thenReturn(value.length)
        whenever(testScenarioState!!.get("key")).thenReturn(value)

        Assertions.assertEquals(value.length, testStepStateValueConverter!!.convert("get(key).(length)"))
    }

    @Test
    fun testGetValueFromStateWhenNotExist() {
        whenever(testScenarioState!!.get("key")).thenReturn(null)

        Assertions.assertThrows(TestDataConversionException::class.java) { testStepStateValueConverter!!.convert("get(key).(length)") }
    }
}