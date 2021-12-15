package org.skellig.teststep.processing.model.factory

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.converter.TestStepValueConverter

internal class TestStepFactoryValueConverterTest {

    private val testStepValueConverter = mock<TestStepValueConverter>()
    private val testStepFactoryConverter = TestStepFactoryValueConverter(testStepValueConverter, null)

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
}