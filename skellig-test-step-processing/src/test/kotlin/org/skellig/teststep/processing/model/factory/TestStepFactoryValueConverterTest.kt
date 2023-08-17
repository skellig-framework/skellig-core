package org.skellig.teststep.processing.model.factory

import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.exception.TestValueConversionException
import org.skellig.teststep.processing.exception.ValueExtractionException
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.utils.UnitTestUtils
import org.skellig.teststep.processing.validation.comparator.DefaultValueComparator
import org.skellig.teststep.processing.value.chunk.RawValueProcessingVisitor
import org.skellig.teststep.processing.value.extractor.DefaultValueExtractor
import org.skellig.teststep.processing.value.function.DefaultFunctionValueExecutor
import org.skellig.teststep.processing.value.property.DefaultPropertyExtractor
import java.util.*

internal class TestStepFactoryValueConverterTest {

    private val EXPECTED_RESULT = "something"
    private val testScenarioState = DefaultTestScenarioState()
    private val converter =
        TestStepFactoryValueConverter.Builder()
            .withValueProcessingVisitor(
                RawValueProcessingVisitor(
                    DefaultFunctionValueExecutor.Builder()
                        .withTestScenarioState(testScenarioState)
                        .withClassPaths(listOf("org.skellig.teststep.processing.model.factory"))
                        .withClassLoader(TestStepFactoryValueConverterTest::class.java.classLoader)
                        .build(),
                    DefaultValueExtractor.Builder().build(),
                    DefaultValueComparator.Builder().build(),
                    DefaultPropertyExtractor(null)
                )
            )
            .build()

    @Test
    fun testConvertWithNoParametersAndDefaultValue() {
        assertEquals("", converter.convertValue<String>("\${p1:}", emptyMap()))
    }

    @Test
    fun testConvertWithParametersAndDefaultValue() {
        val result = "v2"
        assertEquals(result, converter.convertValue<String>("\${p1:}", mapOf(Pair("p1", result))))
    }

//    @Test
//    fun testConvertWithParametersWithNumericName() {
//        val result = "s1"
//        assertEquals(result, converter.convertValue<String>("\${1}", mapOf(Pair("1", result))))
//    }

    @Test
    fun testConvertWithParametersSlashSeparated() {
        assertEquals("/v1/v2", converter.convertValue<String>("/\${p1:data}/\${p2}", mapOf(Pair("p1", "v1"), Pair("p2", "v2"))))
        assertEquals("a / 10 - sample /b", converter.convertValue("a / #[\${key1}] /b", mapOf(Pair("key1", SampleData(10, "sample")))))
    }

    @Test
    fun testConvertSimpleValue() {
        assertEquals("sample", converter.convertValue("#[sample]", emptyMap()))
        assertEquals(EXPECTED_RESULT, converter.convertValue(EXPECTED_RESULT, emptyMap()))
    }

    @Test
    fun testConvertPropertyWithListAsValue() {
        assertEquals(listOf(1, 2, 3), converter.convertValue("#[\${key1}]", mapOf(Pair("key1", listOf(1, 2, 3)))))
    }

    @Test
    fun testConvertWithFunction() {
        val value = SampleData(5, "sample")
        testScenarioState.set("key1", value)

        assertEquals(value.id + value.name.length, converter.convertValue("#[get(key1).getTotalSize()]", emptyMap()))
        assertEquals(value.name + "second name", converter.convertValue("#[get(key1).appendSecondName(second name)]", emptyMap()))
        assertEquals(
            value.name + "a|b|c", converter.convertValue(
                "#[get(key1).mergeNames(\${param1},|)]",
                mapOf(Pair("param1", listOf("a", "b", "c")))
            )
        )
        assertEquals(2, converter.convertValue("#[get(key1).convertToMap(#[true.toBoolean()]).size()]", emptyMap()))
    }

    @Test
    fun testWhenNotDefinedFunction() {
        val func = "f()"
        Assertions.assertThrows(TestValueConversionException::class.java) { converter.convertValue<String>("#[$func]", emptyMap()) }
        Assertions.assertThrows(TestValueConversionException::class.java) { converter.convertValue<String>(func, emptyMap()) }
    }

    @Test
    fun testWhenRunCustomFunction() {
        assertEquals("run successfully", converter.convertValue("#[customFunc()]", emptyMap()))
    }

    @Test
    fun testWhenValueWrappedWithAttachedText() {
        val expected = "1"
        assertEquals("_${expected}_", converter.convertValue("_#[$expected]_", emptyMap()))
    }

    @Test
    fun testWhenValueWithSpecialChars() {
        assertEquals("#[${EXPECTED_RESULT}]", converter.convertValue("'#[${EXPECTED_RESULT}]'", emptyMap()))
    }

    @Test
    fun testWhenSpecialCharsNotInQuotes() {
        testScenarioState.set("1", EXPECTED_RESULT)

        assertEquals("[${EXPECTED_RESULT}]", converter.convertValue<String>("'['#[get(1)]']'", emptyMap()))
    }

    @Test
    fun testWhenNotOddGroups() {
        testScenarioState.set("1", EXPECTED_RESULT)

        assertEquals("a[[]", converter.convertValue<String>("#[a]'[[]'", emptyMap()))
    }

    @Test
    fun testWhenSpecialCharsInValue() {
        assertEquals("1.0", converter.convertValue("'1.0'", emptyMap()))
    }

    @Test
    fun testWhenSpecialCharsInFunction() {
        testScenarioState.set("a", EXPECTED_RESULT)

        assertEquals("thing", converter.convertValue<String>("#[get(a).fromRegex('some([\\\\w\\\\\\'.\\\\\"]+)')]", emptyMap()))
    }

    @Test
    fun testWhenQuotesInRegexForExtraction() {
        testScenarioState.set("a", "__'${EXPECTED_RESULT}'__")

        assertEquals(EXPECTED_RESULT, converter.convertValue<String>("#[get(a).fromRegex('__\\\\\\'(\\\\w+)\\\\\\'__')]", emptyMap()))
    }

    @Test
    fun testWhenFixedCharsInRegexForExtraction() {
        testScenarioState.set("a", "__'${EXPECTED_RESULT}'__")

        assertEquals(listOf("some", "thin"), converter.convertValue<String>("#[get(a).fromRegex('[\\\\w]{4}')]", emptyMap()))
    }

    @Test
    fun testWhenComparatorDefined() {
        assertEquals("match([\\w]{44})", converter.convertValue("match('[\\\\w]{44}')", emptyMap()))
    }

    @Test
    fun testFunctionNotWrapped() {
        testScenarioState.set("1", listOf(EXPECTED_RESULT))

        assertEquals(listOf(EXPECTED_RESULT), converter.convertValue<String>("get(1)", emptyMap()))
    }

    @Test
    fun testWhenTextWithOneQuote() {
        assertEquals("Can't find something", converter.convertValue("Can\\'t find something", emptyMap()))
        assertEquals("one \" two", converter.convertValue("one \\\" two", emptyMap()))
        assertEquals("Can't occupy already taken seats: [s1]", converter.convertValue("'Can\\'t occupy already taken seats: [s1]'", emptyMap()))
    }

    @Test
    fun testCombinationOfInnerWrappedFunctionsAndAttachedTexts() {
        testScenarioState.set("key_1", "key_2")
        testScenarioState.set("key_2", mapOf(Pair("a", mapOf(Pair("b", "key_3")))))
        testScenarioState.set("_key_3_", EXPECTED_RESULT)

        val result = converter.convertValue<String>("prefix: #[get(_#[get(#[get(key_1)]).a.b]_)]\\.", emptyMap())

        assertEquals("prefix: ${EXPECTED_RESULT}.", result)
    }

    @Test
    fun testComplexExtractionPath() {
        assertEquals(9, converter.convertValue("'some.text'.length()", emptyMap()))

        testScenarioState.set("key.1", mapOf(Pair("key.2", mapOf(Pair("key.3", "v")))))

        assertEquals("v", converter.convertValue("get('key.1').'key.2'.'key.3'", emptyMap()))
    }

    @Test
    fun testCombinationOfWrappedFunctionsAndAttachedTexts() {
        testScenarioState.set("1", "a")
        testScenarioState.set("2", listOf(EXPECTED_RESULT))

        val result = converter.convertValue<String>("v1 / #[get(1)] / #[get(2).fromIndex(0).length()] / list", emptyMap())

        assertEquals("v1 / a / ${EXPECTED_RESULT.length} / list", result)
    }

    @Test
    fun testCacheValue() {
        // this is a hack to use parametrised value and return the same from getPropertyFunction
        // in order to check how many times it called
        val value = "\${1}"
        var callCounter = 0
        val testStepFactoryConverter =
            TestStepFactoryValueConverter.Builder()
                .withValueProcessingVisitor(
                    RawValueProcessingVisitor(
                        DefaultFunctionValueExecutor.Builder()
                            .withTestScenarioState(testScenarioState)
                            .build(),
                        DefaultValueExtractor.Builder().build(),
                        mock(),
                        DefaultPropertyExtractor {
                            callCounter++
                            return@DefaultPropertyExtractor value
                        }
                    )
                )
                .build()

        assertEquals(value, testStepFactoryConverter.convertValue<String>(value, emptyMap()))
        assertEquals(value, testStepFactoryConverter.convertValue<String>(value, emptyMap()))
        assertEquals(1, callCounter) // GetPropertyFunction must be called once and second time `convertValue` returns cached value
    }

    @Nested
    inner class SimpleExtractionsTest {

        @Test
        fun testExtractFromJson() {
            val value = "{ \"params\" : { \"f1\" : \"v1\" }}"

            assertEquals("v1", converter.convertValue("\${data}.jsonPath('params.f1')", mapOf(Pair("data", value))))
        }

        @Test
        fun testExtractFromfromRegex() {
            val value = "{ params = { k1 = v1 }}"

            assertEquals("v1", converter.convertValue("\${data}.fromRegex('k1 = (\\\\w+)')", mapOf(Pair("data", value))))
        }

        @Test
        fun testExtractFromMapAndJson() {
            val value = mapOf(Pair("body", "{ \"params\" : { \"f1\" : \"v1\" }}"))

            assertEquals(
                "v1", converter.convertValue(
                    "\${data}.body.jsonPath('params.f1')",
                    mapOf(Pair("data", value))
                )
            )
        }

        @Test
        fun testExtractWhenFunctionNotFound() {
            val value = UnitTestUtils.createMap("k1", "v1")

            val ex = Assertions.assertThrows(ValueExtractionException::class.java) {
                converter.convertValue<String>("\${data}.get", mapOf(Pair("data", value)))
            }

            assertEquals("Failed to find property or method `get` of `class java.util.HashMap`", ex.message)
        }
    }

    @Nested
    inner class CollectionsAndMapExtractionsTest {

        @Test
        @DisplayName("Extract from List of Map last String value")
        fun testExtractFromMapAndList() {
            val testMap = mapOf(Pair("f1", mapOf(Pair("f2", mapOf(Pair("f3", "v3"))))), Pair("f4", listOf(mapOf(Pair("f5", "v5")))))

            assertEquals("v5", converter.convertValue("\${data}.fromIndex(0).f4.fromIndex(0).f5", mapOf(Pair("data", listOf(testMap)))))
        }

        @Test
        @DisplayName("Extract from List last String value")
        fun testExtractFromList() {
            assertEquals("v1", converter.convertValue("\${data}.fromIndex(0)", mapOf(Pair("data", listOf("v1", "v2")))))
        }

        @Test
        @DisplayName("Extract from Array last String value")
        fun testExtractFromArray() {
            assertEquals("v1", converter.convertValue("\${data}.fromIndex(0)", mapOf(Pair("data", arrayOf("v1", "v2")))))
        }

        @Test
        @DisplayName("Extract from List of List last String value")
        fun testExtractFromListOfList() {
            assertEquals("v2", converter.convertValue("\${data}.fromIndex(0).fromIndex(1)", mapOf(Pair("data", listOf(listOf("v1", "v2"))))))
        }

        @Test
        @DisplayName("Extract from object of a class with List and Map inside")
        fun testExtractFromCustomObjectWithListAndMap() {
            val o = listOf(
                Collections.singletonMap<String, Any>(
                    "f1",
                    SimpleObject(mapOf(Pair("f2", TestObject("test"))))
                )
            )

            assertEquals("test", converter.convertValue("\${data}.fromIndex(0).f1.params.f2.name", mapOf(Pair("data", o))))
        }
    }

    @Nested
    inner class ComplexExtractionsTest {

        @Test
        fun testExtractFromManyObjects() {
            val value = SimpleObject(mapOf(Pair("json.body", "{ \"array\" : [ \"1\", \"a=b=ccc\" ]}")))

            assertEquals(
                3, converter.convertValue(
                    "\${data}.params.'json.body'.jsonPath('array[1]').subStringLast(=).length()",
                    mapOf(Pair("data", value))
                )
            )
        }

        @Test
        fun testExtractFromManyObjectsAsList() {
            val value = SimpleObject(mapOf(Pair("data", "f0=0, f1=(v1),f2=v2,f3 = 'v3'")))

            assertEquals(
                listOf("(v1)", "'v3'"), converter.convertValue(
                    "\${data}.params.data.fromRegex('.*f1=(\\\\(\\\\w+\\\\)).*f3 = (\\\\\\'\\\\w+\\\\\\')')",
                    mapOf(Pair("data", value))
                )
            )
        }

        @Test
        fun testExtractFromManyObjectsWithConcatenation() {
            val value = SimpleObject(mapOf(Pair("data", """{ "a": 1 }""".toByteArray())))

            assertEquals(
                "1", converter.convertValue(
                    "\${data}.params.data.toString().jsonPath(a)",
                    mapOf(Pair("data", value))
                )
            )
            assertEquals(
                "1_._", converter.convertValue(
                    "\${data}.params.data.toString(utf8).jsonPath(a).concat('_._')",
                    mapOf(Pair("data", value))
                )
            )
        }

        @Test
        fun testExtractNumericOperations() {
            assertEquals("5", converter.convertValue("2.toLong().plus(3).toString()", emptyMap()))
            assertEquals(25, converter.convertValue("5.toString().times(5)", emptyMap()))
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