package org.skellig.teststep.processing.converter

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.apache.commons.lang3.SerializationUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestDataToBytesConverterTest {

    private val testStepValueConverter = mock<TestStepValueConverter>()
    private val converter = TestDataToBytesConverter(testStepValueConverter)

    @Test
    fun testConvertStringToBytes() {
        val value = "data"
        val testData = mapOf(Pair("toBytes", mapOf(Pair("value", value))))

        whenever(testStepValueConverter.convert(value)).thenReturn(value)

        val result = converter.convert(testData)

        assertEquals(value, String(result as ByteArray))
    }

    @Test
    fun testConvertDataWithFunctionToBytes() {
        val value = "data"
        val convertedData = "convertedData"
        val testData = mapOf(Pair("toBytes", mapOf(Pair("value", value))))

        whenever(testStepValueConverter.convert(value)).thenReturn(convertedData)

        val result = converter.convert(testData)

        assertEquals(convertedData, String(result as ByteArray))
    }

    @Test
    fun testConvertListToBytes() {
        val data = listOf("data")
        val testData = mapOf(Pair("toBytes", mapOf(Pair("value", data))))

        whenever(testStepValueConverter.convert(data)).thenReturn(data)

        val result = converter.convert(testData)

        assertEquals(data, SerializationUtils.deserialize(result as ByteArray?))
    }

    @Test
    fun testConvertInternalDataToBytes() {
        val data = mapOf(Pair("f1", "v1"))
        val testData = mapOf(Pair("toBytes", data))

        whenever(testStepValueConverter.convert(data)).thenReturn(data)

        val result = converter.convert(testData)

        assertEquals(data, SerializationUtils.deserialize(result as ByteArray?))
    }
}