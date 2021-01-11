package org.skellig.teststep.processing.converter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class TestDataToJsonConverterTest {

    private val converter = TestDataToJsonConverter()

    @Test
    internal fun testConvertToEmptyMapJson() {
        val testData = mapOf<Any, Any>(Pair("json", mapOf<Any, Any>()))

        assertEquals("{}", converter.convert(testData))
    }

    @Test
    internal fun testConvertToSimpleJson() {
        val testData = mapOf<Any, Any>(Pair("json", mapOf<Any, Any>(Pair("f1", "v1"), Pair("f2", 2))))

        assertEquals("{\"f1\":\"v1\",\"f2\":2}", converter.convert(testData))
    }

    @Test
    internal fun testConvertToComplexJson() {
        val testData = mapOf(Pair("json",
                mapOf(Pair("f1", listOf(mapOf(Pair("a", "b")))),
                        Pair("f2", mapOf(Pair("c", "d"))))))

        assertEquals("{\"f1\":[{\"a\":\"b\"}],\"f2\":{\"c\":\"d\"}}", converter.convert(testData))
    }

    @Test
    internal fun testConvertToListJson() {
        val testData = mapOf(Pair("json", listOf("a", "b", "c")))

        assertEquals("[\"a\",\"b\",\"c\"]", converter.convert(testData))
    }
}