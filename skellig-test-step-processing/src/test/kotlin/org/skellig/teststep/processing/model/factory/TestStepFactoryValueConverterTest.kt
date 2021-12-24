package org.skellig.teststep.processing.model.factory

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor

internal class TestStepFactoryValueConverterTest {

    private val testStepValueConverter = mock<TestStepValueConverter>()
    private val valueExtractor = mock<TestStepValueExtractor>()
    private val testStepFactoryConverter =
        TestStepFactoryValueConverter.Builder()
            .withValueConverter(testStepValueConverter)
            .withTestStepValueExtractor(valueExtractor)
            .build()

    @Test
    fun testConvertWithNoParametersAndDefaultValue() {
        val result = ""
        whenever(testStepValueConverter.convert(result)).thenReturn(result)

        assertEquals(result, testStepFactoryConverter.convertValue<String>("\${p1:}", emptyMap()))
    }

    @Test
    fun testConvertWithParametersAndDefaultValue() {
        val result = "v2"
        whenever(testStepValueConverter.convert(result)).thenReturn(result)

        assertEquals(result, testStepFactoryConverter.convertValue<String>("\${p1:}", mapOf(Pair("p1", "v2"))))
    }

    @Test
    fun testConvertWithParametersSlashSeparated() {
        val parameters = mapOf(Pair("p1", "v1"), Pair("p2", "v2"))

        whenever(testStepValueConverter.convert("/v1/v2")).thenReturn("/v1/v2")

        assertEquals("/v1/v2", testStepFactoryConverter.convertValue<String>("/\${p1:data}/\${p2}", parameters))
    }

    @Test
    fun testCacheValue() {
        // this is a hack to use parametrised value and return the same from getPropertyFunction
        // in order to check how many times it called
        val value = "\${1}"
        var callCounter = 0
        val testStepFactoryConverter =
            TestStepFactoryValueConverter.Builder()
                .withValueConverter(testStepValueConverter)
                .withTestStepValueExtractor(valueExtractor)
                .withGetPropertyFunction {
                    callCounter++
                    return@withGetPropertyFunction value
                }
                .build()

        whenever(testStepValueConverter.convert(value)).thenReturn(value)

        assertEquals(value, testStepFactoryConverter.convertValue<String>(value, emptyMap()))
        assertEquals(value, testStepFactoryConverter.convertValue<String>(value, emptyMap()))
        assertEquals(1, callCounter) // GetPropertyFunction must be called once and second time `convertValue` returns cached value
    }
}