package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.value.exception.FunctionExecutionException

class JsonToValueExtractorTest {

    private val jsonToMap = JsonToMapTestStepValueExtractor()
    private val jsonToList = JsonToListTestStepValueExtractor()

    @Test
    fun testFromEmptyJsonToMap() {
        assertEquals(mapOf<Any, Any>(), jsonToMap.execute("jsonToMap", "{}", emptyArray()))
    }

    @Test
    fun testFromSimpleJsonToMap() {
        assertEquals(
            mapOf<Any, Any>(
                Pair("f1", "v1"),
                Pair("f2", listOf("a", "b")),
                Pair("f3", mapOf(Pair("f4", 10))),
            ),
            jsonToMap.execute(
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
            jsonToList.execute(
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
        assertEquals(emptyMap<Any, Any>(), jsonToMap.execute("jsonToMap", null, emptyArray()))
        assertEquals(emptyList<Any>(), jsonToList.execute("jsonToList", null, emptyArray()))

        assertEquals(emptyMap<Any, Any>(), jsonToMap.execute("jsonToMap", "", emptyArray()))
        assertEquals(emptyList<Any>(), jsonToList.execute("jsonToList", "", emptyArray()))
    }

    @Test
    fun testFromInvalidJson() {
        val ex = assertThrows<FunctionExecutionException> { jsonToList.execute("jsonToList", "{ a }", emptyArray()) }

        assertEquals("Failed to convert JSON to List: '{ a }'", ex.message)
    }

}