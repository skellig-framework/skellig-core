package org.skellig.teststep.processing.value.extractor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.value.extractor.exception.ValueExtractionException

class JsonToValueExtractorTest {

    private val jsonToMap = JsonToMapTestStepValueExtractor()
    private val jsonToList = JsonToListTestStepValueExtractor()

    @Test
    fun testFromEmptyJsonToMap() {
        assertEquals(mapOf<Any, Any>(), jsonToMap.extractFrom("jsonToMap", "{}", emptyArray()))
    }

    @Test
    fun testFromSimpleJsonToMap() {
        assertEquals(
            mapOf<Any, Any>(
                Pair("f1", "v1"),
                Pair("f2", listOf("a", "b")),
                Pair("f3", mapOf(Pair("f4", 10))),
            ),
            jsonToMap.extractFrom(
                "jsonToMap",
                """
                {
                   "f1": "v1",
                   "f2": [ "a","b"],
                   "f3": {
                      "f4": 10
                   }
                }
                """.trimIndent(), emptyArray()
            )
        )
    }

    @Test
    fun testFromSimpleJsonToList() {
        assertEquals(
            listOf(
                mapOf(Pair("f1", "v1"), Pair("f2", "v2")),
                mapOf(Pair("f1", "v3"), Pair("f2", "v4"))
            ),
            jsonToList.extractFrom(
                "jsonToList",
                """
                [
                    {
                      "f1": "v1",
                      "f2": "v2"
                    },
                    {
                      "f1": "v3",
                      "f2": "v4"
                    }
                ]
                """.trimIndent(), emptyArray()
            )
        )
    }

    @Test
    fun testFromNullOrEmptyStringJson() {
        assertEquals(emptyMap<Any, Any>(), jsonToMap.extractFrom("jsonToMap", null, emptyArray()))
        assertEquals(emptyList<Any>(), jsonToList.extractFrom("jsonToList", null, emptyArray()))

        assertEquals(emptyMap<Any, Any>(), jsonToMap.extractFrom("jsonToMap", "", emptyArray()))
        assertEquals(emptyList<Any>(), jsonToList.extractFrom("jsonToList", "", emptyArray()))
    }

    @Test
    fun testFromInvalidJson() {
        val ex = assertThrows<ValueExtractionException> { jsonToList.extractFrom("jsonToList", "{ a }", emptyArray()) }

        assertEquals("Failed to convert JSON to List: '{ a }'", ex.message)
    }

}