package org.skellig.teststep.processing.converter

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.exception.TestValueConversionException
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor
import java.time.LocalDateTime

class PropertyValueConverterTest {

    companion object {
        private const val CUSTOM_PROPERTY_KEY = "custom.properties_1"
        private const val CUSTOM_PROPERTY_KEY_2 = "custom.properties_2"
        private const val KEY_ONE = "1"
        private const val KEY_TWO = "2"
        private const val KEY_REF = "a-b-1"
        private const val DEFAULT_CUSTOM_PROPERTY_VALUE = "from custom properties"
        private const val DEFAULT_CUSTOM_PROPERTY_VALUE_2 = "from custom properties 2"
    }

    private var valueConverter: PropertyValueConverter? = null
    private var testScenarioState: TestScenarioState? = null
    private var valueExtractor: TestStepValueExtractor? = null

    @BeforeEach
    fun setUp() {
        testScenarioState = mock()
        valueExtractor = mock()
        val converters = mutableListOf(
            TestStepStateValueConverter(testScenarioState!!, valueExtractor),
            CurrentDateTimeValueConverter()
        )

        valueConverter = PropertyValueConverter(
            object : TestStepValueConverter {
                override fun convert(value: Any?): Any? {
                    var newValue: Any? = value
                    for (valueConverter in converters) {
                        if (newValue is String)
                            newValue = valueConverter.convert(newValue.toString())
                    }
                    return newValue
                }
            }
        ) { when(it) {
            CUSTOM_PROPERTY_KEY -> DEFAULT_CUSTOM_PROPERTY_VALUE
            CUSTOM_PROPERTY_KEY_2 -> DEFAULT_CUSTOM_PROPERTY_VALUE_2
            KEY_REF -> CUSTOM_PROPERTY_KEY
            KEY_ONE -> "a"
            KEY_TWO -> "b"
            else -> null
        } }

        converters.add(0, valueConverter!!) // to enable to get inner property values
    }

    @Test
    fun testSimpleParameterWithNoValue() {
        Assertions.assertThrows(TestValueConversionException::class.java) {
            valueConverter!!.convert("\${a}")
        }
    }

    @Test
    fun testExistingParameterButWithExtraSpaceAfter() {
        Assertions.assertThrows(TestValueConversionException::class.java) {
            valueConverter!!.convert("\${1  }")
        }
    }

    @Test
    fun testExistingParameterButWithExtraSpaceBefore() {
        Assertions.assertThrows(TestValueConversionException::class.java) {
            valueConverter!!.convert("\${  1}")
        }
    }

    @Test
    fun testSimpleParameterWithNoValueAndDefault() {
        Assertions.assertEquals("def", valueConverter!!.convert("\${a:def}"))
    }

    @Test
    fun testTwoParametersMerged() {
        Assertions.assertEquals("ab", valueConverter!!.convert("\${1}\${2}"))
    }

    @Test
    fun testSimpleParameterWithNoValueAndDefaultIsNull() {
        Assertions.assertNull(valueConverter!!.convert("\${a:null}"))
    }

    @Test
    fun testSimpleParameterWithNoValueAndDefaultIsEmpty() {
        Assertions.assertEquals("", valueConverter!!.convert("\${a:}"))
    }

    @Test
    fun testConvertNull() {
        Assertions.assertNull(valueConverter!!.convert(null))
    }

    @Test
    fun testWithNestedParametersAndDefault() {
        Assertions.assertEquals("v3", valueConverter!!.convert("\${key_1 : \${key_2 : v3}}"))
    }

//    @Test
//    fun testWithNestedParametersAndDefaultWhenFirstIsEmpty() {
//        whenever(testScenarioState!!.get("id")).thenReturn("")
//        Assertions.assertEquals("v3", valueConverter!!.convert("\${id : v3}"))
//    }

    @Test
    fun testWithComplexNestedParametersAndAttachedText() {
        Assertions.assertEquals("id:v3_end", valueConverter!!.convert("\${key_1 : id:\${key_2 : \${id:v3}_end}}"))
    }

    @Test
    fun testWithSpecialCharsAsValue() {
        Assertions.assertEquals("{a} - \${:)}} $1 \\", valueConverter!!.convert("\\{\${$KEY_ONE}\\} - $\\{\\:)\\}\\} $1 \\"))
    }

    @Test
    fun testWithComplexNestedParametersAndAttachedText2() {
        //
        Assertions.assertEquals("prefix_" +
                " __b" + // only 1 space is removed after ':'
                " __ " + // preserve spaces after '}' and before '${'
                "a__" + // only 1 spaces is removed for key '1', before '}'
                " / get(a) / v1 / " + // all spaces must be preserved as nothing is inside '{ }'
                " ",  // 1 space is removed after ':' and another space removed before '}'
            valueConverter!!.convert("prefix_\${a :  __\${ $KEY_TWO : 10 } __ \${$KEY_ONE }__} / get(\${$KEY_ONE}) / v1 / \${ b:   }"))
    }

//    @Test
    fun testWithNestedFunctionsWithAttachedText() {
        whenever(testScenarioState!!.get("id")).thenReturn("10")
        Assertions.assertEquals(
            "_10_-$DEFAULT_CUSTOM_PROPERTY_VALUE",
            valueConverter!!.convert("\${key_1 : _get(id)_-\${$CUSTOM_PROPERTY_KEY}}")
        )
    }

    @Test
    fun testWithManyParametersAndAttachedText() {
        Assertions.assertEquals(
            "/$DEFAULT_CUSTOM_PROPERTY_VALUE/$DEFAULT_CUSTOM_PROPERTY_VALUE_2/get",
            valueConverter!!.convert("/\${$CUSTOM_PROPERTY_KEY:default}/\${$CUSTOM_PROPERTY_KEY_2}/get"))
    }

//    @Test
//    fun testWithNestedFunctionReturningObject() {
//        Assertions.assertEquals(LocalDateTime::class.java, valueConverter!!.convert("\${key_1 : now(UTC)}")!!.javaClass)
//    }

//    @Test
    fun testWithNestedFunctionReturningObjectWithNoKey() {
        val expectedData = listOf(1, 2)
        whenever(testScenarioState!!.get("id")).thenReturn(expectedData)
        whenever(valueExtractor!!.extract(expectedData, "size")).thenReturn(expectedData.size)

        Assertions.assertEquals(expectedData.size, valueConverter!!.convert("\${get(id).size}"))
        Assertions.assertEquals("__${expectedData.size}dot", valueConverter!!.convert("__\${get(id).size}dot"))
    }

    @Test
    fun testWithCustomProperties() {
        Assertions.assertEquals(
            "$DEFAULT_CUSTOM_PROPERTY_VALUE - $DEFAULT_CUSTOM_PROPERTY_VALUE",
            valueConverter!!.convert("\${$CUSTOM_PROPERTY_KEY} - \${$CUSTOM_PROPERTY_KEY}")
        )
    }

    @Test
    fun testWithSystemProperties() {
        val key = "key1"
        System.setProperty(key, "v1")

        Assertions.assertEquals(System.getProperty(key), valueConverter!!.convert("\${$key}"))
    }
}