package org.skellig.teststep.processing.converter

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.model.factory.TestStepFactoryValueConverter
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.valueextractor.DefaultValueExtractor

class DefaultValueConverterTest {

    companion object {
        const val EXPECTED_RESULT = "something"
    }

    private val testScenarioState = mock<TestScenarioState>()
    private val converter =
        DefaultValueConverter.Builder()
            .withTestScenarioState(testScenarioState)
            .withTestStepValueExtractor(DefaultValueExtractor.Builder().build())
            .build()

    @Test
    fun testWhenNoFunctions() {
        assertEquals(EXPECTED_RESULT, converter.convert(EXPECTED_RESULT))
    }

    @Test
    fun testWhenNotDefinedFunction() {
        val expected = "f()"
        assertEquals(expected, converter.convert(expected))
    }

    @Test
    fun testWhenNotDefinedFunctionWrapped() {
        val expected = "f()"
        assertEquals(expected, converter.convert("#[$expected]"))
    }

    @Test
    fun testWhenValueWrappedWithAttachedText() {
        val expected = "1"
        assertEquals("_${expected}_", converter.convert("_#[$expected]_"))
    }

    @Test
    fun testWhenValueWithSpecialChars() {
        assertEquals("#[$EXPECTED_RESULT]", converter.convert("'#[$EXPECTED_RESULT]'"))
        assertEquals("#[$EXPECTED_RESULT]", converter.convert("\"#[$EXPECTED_RESULT]\""))
    }

    @Test
    fun testWhenSpecialCharsNotInQuotes() {
        whenever(testScenarioState.get("1")).thenReturn(EXPECTED_RESULT)

        val result = converter.convert("[get(1)]")

        assertEquals("[$EXPECTED_RESULT]", result)
    }

    @Test
    fun testWhenNotOddGroups() {
        whenever(testScenarioState.get("1")).thenReturn(EXPECTED_RESULT)

        val result = converter.convert("#[a][[]")

        assertEquals("a[[]", result)
    }

    @Test
    fun testWhenSpecialCharsInValue() {
        assertEquals("1.0", converter.convert("\"1.0\""))
    }

    @Test
    fun testWhenSpecialCharsInFunction() {
        whenever(testScenarioState.get("a")).thenReturn(EXPECTED_RESULT)

        val result = converter.convert("#[get(a).regex('some([\\w\\'.\\\"]+)')]")

        assertEquals("thing", result)
    }

    @Test
    fun testWhenQuotesInRegexForExtraction() {
        whenever(testScenarioState.get("a")).thenReturn("__'$EXPECTED_RESULT'__")

        val result = converter.convert("#[get(a).regex('__\\'(\\w+)\\'__')]")

        assertEquals(EXPECTED_RESULT, result)
    }

    @Test
    fun testWhenFixedCharsInRegexForExtraction() {
        whenever(testScenarioState.get("a")).thenReturn("__'$EXPECTED_RESULT'__")

        val result = converter.convert("#[get(a).regex('[\\w]{4}')]")

        assertEquals(listOf("some", "thin"), result)
    }

    @Test
    fun testWhenSpecialCharsInRegexNotInQuotes() {
        assertEquals("match([\\w]{44})", converter.convert("match([\\w]{44})"))
    }

    @Test
    fun testFunctionNotWrapped() {
        whenever(testScenarioState.get("1")).thenReturn(listOf(EXPECTED_RESULT))

        val result = converter.convert("get(1)")

        assertEquals(listOf(EXPECTED_RESULT), result)
    }

    @Test
    fun testWhenTextWithOneQuote() {
        assertEquals("Can't find something", converter.convert("Can\\'t find something"))
        assertEquals("one \" two", converter.convert("one \\\" two"))
        assertEquals("Can't occupy already taken seats: [s1]", converter.convert("Can\\'t occupy already taken seats: [s1]"))
    }

    @Test
    fun testCombinationOfInnerWrappedFunctionsAndAttachedTexts() {
        whenever(testScenarioState.get("key_1")).thenReturn("key_2")
        whenever(testScenarioState.get("key_2")).thenReturn(mapOf(Pair("a", mapOf(Pair("b", "key_3")))))
        whenever(testScenarioState.get("_key_3_")).thenReturn(EXPECTED_RESULT)

        val result = converter.convert("prefix: #[get(_#[get(#[get(key_1)]).a.b]_)].")

        assertEquals("prefix: $EXPECTED_RESULT.", result)
    }

    @Test
    fun testComplexExtractionPath() {
        assertEquals(9, converter.convert("'some.text'.length"))

        whenever(testScenarioState.get("key.1")).thenReturn(mapOf(Pair("key.2", mapOf(Pair("key.3", "v")))))

        assertEquals("v", converter.convert("get('key.1').'key.2'.'key.3'"))
    }

    @Test
    fun testCombinationOfWrappedFunctionsAndAttachedTexts() {
        whenever(testScenarioState.get("1")).thenReturn("a")
        whenever(testScenarioState.get("2")).thenReturn(listOf(EXPECTED_RESULT))

        val result = converter.convert("v1 / #[get(1)] / #[get(2).fromIndex(0).length] / list")

        assertEquals("v1 / a / ${EXPECTED_RESULT.length} / list", result)
    }

    @Test
    fun testCacheValue() {
        val value = "get(100)"
        // return the same value in order to check if it was cached on second call
        whenever(testScenarioState.get("100")).thenReturn(value)

        assertEquals(value, converter.convert(value))
        assertEquals(value, converter.convert(value))
        verify(testScenarioState, times(1)).get("100")
    }
}