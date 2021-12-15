package org.skellig.teststep.processing.valueextractor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JsonToTestStepValueExtractorTest {

    private val jsonToMap = JsonToMapTestStepValueExtractor()
    private val jsonToList = JsonToListTestStepValueExtractor()

    @Test
    fun testFromEmptyJsonToMap() {
        assertEquals(mapOf<Any, Any>(), jsonToMap.extract("{}", null))
    }

    @Test
    fun testFromSimpleJsonToMap() {
        assertEquals(mapOf<Any, Any>(
            Pair("f1", "v1"),
            Pair("f2", listOf("a", "b")),
            Pair("f3", mapOf(Pair("f4", 10))),
        ),
            jsonToMap.extract(
                """
                {
                   "f1": "v1",
                   "f2": [ "a","b"],
                   "f3": {
                      "f4": 10
                   }
                }
                """.trimIndent(), null))
    }

    @Test
    fun testFromSimpleJsonToList() {
        assertEquals(
            listOf(
               mapOf(Pair("f1", "v1"), Pair("f2", "v2")),
               mapOf(Pair("f1", "v3"), Pair("f2", "v4"))
            ),
            jsonToList.extract(
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
                """.trimIndent(), null))
    }

    @Test
    fun testFromNullOrEmptyStringJson() {
        assertEquals(emptyMap<Any, Any>(), jsonToMap.extract(null, null))
        assertEquals(emptyList<Any>(), jsonToList.extract(null, null))

        assertEquals(emptyMap<Any, Any>(), jsonToMap.extract("", null))
        assertEquals(emptyList<Any>(), jsonToList.extract("", null))
    }

}