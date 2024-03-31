package org.skellig.teststep.processing.value

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.value.function.DefaultFunctionValueExecutor
import org.skellig.teststep.processing.value.property.DefaultPropertyExtractor
import org.skellig.teststep.reader.value.expression.*

class ValueExpressionContextFactoryTest {

    private val expectedResult = "something"
    private val testScenarioState = DefaultTestScenarioState()
    private val factory =
        ValueExpressionContextFactory(
            DefaultFunctionValueExecutor.Builder()
                .withTestScenarioState(testScenarioState)
                .withClassPaths(listOf("org.skellig.teststep.processing.value"))
                .withClassLoader(ValueExpressionContextFactoryTest::class.java.classLoader)
                .build(),
            DefaultPropertyExtractor(null)
        )

    @Test
    fun testConvertWithNoParametersAndNoDefault() {
        assertNull(PropertyValueExpression("p1", null).evaluate(factory.create(emptyMap())))
    }

    @Test
    fun testConvertWithNoParametersAndDefaultValue() {
        assertEquals("", PropertyValueExpression("p1", StringValueExpression("")).evaluate(factory.create(emptyMap())))
    }

    @Test
    fun testConvertWithParametersAndDefaultValue() {
        val result = "v2"
        assertEquals(result, PropertyValueExpression("p1", StringValueExpression("")).evaluate(factory.create(mapOf(Pair("p1", result)))))
    }

    @Test
    fun testConvertWithParametersWithNumericName() {
        val result = "s1"
        assertEquals(result, PropertyValueExpression("1", StringValueExpression("")).evaluate(factory.create(mapOf(Pair("1", result)))))
    }

    @Test
    fun testConvertWithParametersSlashSeparated() {
        assertEquals(
            "/v1/v2",
            MathOperationExpression(
                "+",
                MathOperationExpression(
                    "+",
                    MathOperationExpression("+", StringValueExpression("/"), PropertyValueExpression("p1", StringValueExpression("data"))),
                    StringValueExpression("/")
                ),
                PropertyValueExpression("p2", null)
            ).evaluate(factory.create(mapOf(Pair("p1", "v1"), Pair("p2", "v2"))))
        )
    }

    @Test
    fun testConvertSimpleValue() {
        assertEquals(expectedResult, StringValueExpression(expectedResult).evaluate(factory.create(emptyMap())))
    }

    @Test
    fun testConvertPropertyWithListAsValue() {
        assertEquals(listOf(1, 2, 3), PropertyValueExpression("key1", null).evaluate(factory.create(mapOf(Pair("key1", listOf(1, 2, 3))))))
    }

    @Test
    fun testConvertWithFunction() {
        val value = SampleData(5, "sample")
        testScenarioState.set("key1", value)

        assertEquals(
            value.id + value.name.length,
            CallChainExpression(
                listOf(
                    FunctionCallExpression("get", arrayOf(StringValueExpression("key1"))),
                    FunctionCallExpression("getTotalSize", emptyArray())
                )
            ).evaluate(factory.create(emptyMap()))
        )

        assertEquals(
            value.name + "second name",
            CallChainExpression(
                listOf(
                    FunctionCallExpression("get", arrayOf(StringValueExpression("key1"))),
                    FunctionCallExpression("appendSecondName", arrayOf(AlphanumericValueExpression("second name")))
                )
            ).evaluate(factory.create(emptyMap()))
        )

        assertEquals(
            value.name + "a|b|c",
            CallChainExpression(
                listOf(
                    FunctionCallExpression("get", arrayOf(StringValueExpression("key1"))),
                    FunctionCallExpression("mergeNames", arrayOf(PropertyValueExpression("param1", null), StringValueExpression("|")))
                )
            ).evaluate(
                factory.create(mapOf(Pair("param1", listOf("a", "b", "c"))))
            )
        )

        assertEquals(
            2,
            CallChainExpression(
                listOf(
                    FunctionCallExpression("get", arrayOf(StringValueExpression("key1"))),
                    FunctionCallExpression("convertToMap", arrayOf(BooleanValueExpression("true"))),
                    FunctionCallExpression("size", emptyArray())
                )
            ).evaluate(factory.create(emptyMap()))
        )
    }

    @Test
    fun testWhenNotDefinedFunction() {
        val func = "f()"
        assertThrows(FunctionExecutionException::class.java) { FunctionCallExpression(func, emptyArray()).evaluate(factory.create(emptyMap())) }
    }

    @Test
    fun testWhenRunCustomFunction() {
        assertEquals("run successfully", FunctionCallExpression("customFunc", emptyArray()).evaluate(factory.create(emptyMap())))
    }


    @Test
    fun testWhenValueWithSpecialChars() {
        assertEquals("#[${expectedResult}]", StringValueExpression("#[${expectedResult}]").evaluate(factory.create(emptyMap())))
    }


    @Test
    fun testWhenSpecialCharsInFunction() {
        testScenarioState.set("a", expectedResult)

        assertEquals(
            "thing", CallChainExpression(
                listOf(
                    FunctionCallExpression("get", arrayOf(AlphanumericValueExpression("a"))),
                    FunctionCallExpression("fromRegex", arrayOf(StringValueExpression("some([\\w\\\\'.\\\"]+)")))
                )
            ).evaluate(factory.create(emptyMap()))
        )
    }

    @Test
    fun testWhenQuotesInRegexForExtraction() {
        testScenarioState.set("a", "__\"${expectedResult}\"__")

        assertEquals(
            expectedResult, CallChainExpression(
                listOf(
                    FunctionCallExpression("get", arrayOf(AlphanumericValueExpression("a"))),
                    FunctionCallExpression("fromRegex", arrayOf(StringValueExpression("__\\\"(\\w+)\\\"__")))
                )
            ).evaluate(factory.create(emptyMap()))
        )
    }


    @Test
    fun testWhenTextWithOneQuote() {
        assertEquals("Can\"t find something", StringValueExpression("Can\"t find something").evaluate(factory.create(emptyMap())))
    }

    @Test
    fun testComplexExtractionPath() {
        assertEquals(9, CallChainExpression(listOf(StringValueExpression("some.text"), FunctionCallExpression("length", emptyArray()))).evaluate(factory.create(emptyMap())))

        testScenarioState.set("key.1", mapOf(Pair("key.2", mapOf(Pair("key.3", "v")))))

        assertEquals(
            "v", CallChainExpression(
                listOf(
                    FunctionCallExpression("get", arrayOf(StringValueExpression("key.1"))),
                    StringValueExpression("key.2"),
                    StringValueExpression("key.3")
                )
            ).evaluate(factory.create(emptyMap()))
        )
    }

    @Test
    fun testIdempotencyOfConvert() {
        val key = "1"
        val value = "v1"

        assertNull(PropertyValueExpression(key, null).evaluate(factory.create(emptyMap())))
        assertEquals(value, PropertyValueExpression(key, null).evaluate(factory.create(mapOf(Pair(key, value)))))
        assertEquals(value, PropertyValueExpression(key, null).evaluate(factory.create(mapOf(Pair(key, value)))))
    }

    @Nested
    inner class SimpleExtractionsTest {

        @Test
        fun testExtractFromJson() {
            val value = "{ \"params\" : { \"f1\" : \"v1\" }}"

            assertEquals(
                "v1", CallChainExpression(
                    listOf(
                        PropertyValueExpression("data", null),
                        FunctionCallExpression("jsonPath", arrayOf(StringValueExpression("params.f1"))),
                    )
                ).evaluate(factory.create(mapOf(Pair("data", value))))
            )
        }

        @Test
        fun testExtractFromRegex() {
            val value = "{ params = { k1 = v1 }}"

            assertEquals(
                "v1", CallChainExpression(
                    listOf(
                        PropertyValueExpression("data", null),
                        FunctionCallExpression("fromRegex", arrayOf(StringValueExpression("k1 = (\\w+)"))),
                    )
                ).evaluate(factory.create(mapOf(Pair("data", value))))
            )
        }

        @Test
        fun testExtractFromMapAndJson() {
            val value = mapOf(Pair("body", "{ \"params\" : { \"f1\" : \"v1\" }}"))

            assertEquals(
                "v1", CallChainExpression(
                    listOf(
                        PropertyValueExpression("data", null),
                        AlphanumericValueExpression("body"),
                        FunctionCallExpression("jsonPath", arrayOf(StringValueExpression("params.f1"))),
                    )
                ).evaluate(factory.create(mapOf(Pair("data", value))))
            )
        }

        @Test
        fun testExtractWhenFunctionNotFound() {
            val value = mapOf(Pair("k1", "v1"))

            val ex = assertThrows(FunctionExecutionException::class.java) {
                CallChainExpression(
                    listOf(
                        PropertyValueExpression("data", null),
                        FunctionCallExpression("getAny", emptyArray())
                    ),
                ).evaluate(factory.create(mapOf(Pair("data", value))))
            }

            assertEquals("No function or property `getAny` found in the result `{k1=v1}` with argument pairs ()", ex.message)
        }
    }

    @Nested
    inner class CollectionsAndMapExtractionsTest {

        @Test
        @DisplayName("Extract from List of Map last String value")
        fun testExtractFromMapAndList() {
            val testMap = mapOf(Pair("f1", mapOf(Pair("f2", mapOf(Pair("f3", "v3"))))), Pair("f4", listOf(mapOf(Pair("f5", "v5")))))

            assertEquals(
                "v5", CallChainExpression(
                    listOf(
                        PropertyValueExpression("data", null),
                        FunctionCallExpression("fromIndex", arrayOf(NumberValueExpression("0"))),
                        AlphanumericValueExpression("f4[0]"),
                        AlphanumericValueExpression("f5"),

                        )
                ).evaluate(factory.create(mapOf(Pair("data", listOf(testMap)))))
            )
        }

        @Test
        @DisplayName("Extract from List last String value")
        fun testExtractFromList() {
            assertEquals(
                "v1", CallChainExpression(
                    listOf(
                        PropertyValueExpression("data", null),
                        FunctionCallExpression("fromIndex", arrayOf(NumberValueExpression("0")))
                    )
                ).evaluate(factory.create(mapOf(Pair("data", listOf("v1", "v2")))))
            )
        }

        @Test
        @DisplayName("Extract from Array last String value")
        fun testExtractFromArray() {
            assertEquals(
                "v1", CallChainExpression(
                    listOf(
                        PropertyValueExpression("data", null),
                        FunctionCallExpression("fromIndex", arrayOf(NumberValueExpression("0"))),

                        )
                ).evaluate(factory.create(mapOf(Pair("data", arrayOf("v1", "v2")))))
            )
        }

        @Test
        @DisplayName("Extract from List of List last String value")
        fun testExtractFromListOfList() {
            assertEquals(
                "v2", CallChainExpression(
                    listOf(
                        PropertyValueExpression("data", null),
                        FunctionCallExpression("fromIndex", arrayOf(NumberValueExpression("0"))),
                        FunctionCallExpression("fromIndex", arrayOf(NumberValueExpression("1"))),

                        )
                ).evaluate(factory.create(mapOf(Pair("data", listOf(listOf("v1", "v2"))))))
            )
        }

        @Test
        @DisplayName("Extract from object of a class with List and Map inside")
        fun testExtractFromCustomObjectWithListAndMap() {
            val value = listOf(mapOf(Pair("f1", SimpleObject(mapOf(Pair("f2", TestObject("test")))))))

            assertEquals(
                "test", CallChainExpression(
                    listOf(
                        PropertyValueExpression("data", null),
                        FunctionCallExpression("fromIndex", arrayOf(NumberValueExpression("0"))),
                        AlphanumericValueExpression("f1"),
                        AlphanumericValueExpression("params"),
                        AlphanumericValueExpression("f2"),
                        AlphanumericValueExpression("name"),
                    )
                ).evaluate(factory.create(mapOf(Pair("data", value))))
            )
        }
    }

    @Nested
    inner class ComplexCallChainTest {

        @Test
        fun testExtractFromManyObjects() {
            val value = SimpleObject(mapOf(Pair("json.body", "{ \"array\" : [ \"1\", \"a=b=ccc\" ]}")))

            assertEquals(
                3, CallChainExpression(
                    listOf(
                        PropertyValueExpression("data", null),
                        AlphanumericValueExpression("params"),
                        StringValueExpression("json.body"),
                        FunctionCallExpression("jsonPath", arrayOf(StringValueExpression("array[1]"))),
                        FunctionCallExpression("subStringLast", arrayOf(StringValueExpression("="))),
                        FunctionCallExpression("length", emptyArray()),
                    )
                ).evaluate(factory.create(mapOf(Pair("data", value))))
            )
        }

        @Test
        fun testExtractFromManyObjectsAsList() {
            val value = SimpleObject(mapOf(Pair("data", "f0=0, f1=(v1),f2=v2,f3 = 'v3'")))

            assertEquals(
                listOf("(v1)", "'v3'"),
                CallChainExpression(
                    listOf(
                        PropertyValueExpression("data", null),
                        AlphanumericValueExpression("params"),
                        StringValueExpression("data"),
                        FunctionCallExpression("fromRegex", arrayOf(StringValueExpression(".*f1=(\\(\\w+\\)).*f3 = ('\\w+')")))
                    )
                ).evaluate(factory.create(mapOf(Pair("data", value))))
            )
        }

        @Test
        fun testExtractFromManyObjectsWithConcatenation() {
            val value = SimpleObject(mapOf(Pair("data", """{ "a": 1 }""".toByteArray())))

            assertEquals(
                "1",
                CallChainExpression(
                    listOf(
                        PropertyValueExpression("data", null),
                        AlphanumericValueExpression("params"),
                        StringValueExpression("data"),
                        FunctionCallExpression("toString", emptyArray()),
                        FunctionCallExpression("jsonPath", arrayOf(AlphanumericValueExpression("a"))),

                        )
                ).evaluate(factory.create(mapOf(Pair("data", value))))
            )

            assertEquals(
                "1_._",
                CallChainExpression(
                    listOf(
                        PropertyValueExpression("data", null),
                        AlphanumericValueExpression("params"),
                        StringValueExpression("data"),
                        FunctionCallExpression("toString", arrayOf(AlphanumericValueExpression("utf8"))),
                        FunctionCallExpression("jsonPath", arrayOf(AlphanumericValueExpression("a"))),
                        FunctionCallExpression("concat", arrayOf(AlphanumericValueExpression("_._"))),

                        )
                ).evaluate(factory.create(mapOf(Pair("data", value))))
            )
        }

    }

    inner class SimpleObject(val params: Map<String, Any?>)

    inner class TestObject(val name: String)
}

class SampleData(var id: Int, val name: String) {

    override fun toString(): String {
        return "$id - $name"
    }

    fun getTotalSize(): Int = id + name.length

    fun appendSecondName(name: String): String = this.name + name

    fun mergeNames(names: List<String>, delimiter: String): String = this.name + names.joinToString(delimiter)

    fun convertToMap(includeSelf: Boolean): Map<String, Any> =
        if (includeSelf) mapOf(Pair(name, id), Pair("self", this))
        else mapOf(Pair(name, id))
}

class CustomFunctions {

    @org.skellig.teststep.processing.value.function.Function
    fun customFunc(): String = "run successfully"

}