package org.skellig.teststep.processing.converter

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor
import java.time.LocalDateTime

class PropertyValueConverterTest {

    companion object {
        private const val CUSTOM_PROPERTY_KEY = "custom.properties_1"
        private const val DEFAULT_CUSTOM_PROPERTY_VALUE = "from custom properties"
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
                override fun convert(value: String?): Any? {
                    var newValue: Any? = value
                    for (valueConverter in converters) {
                        if (newValue is String)
                            newValue = valueConverter.convert(newValue.toString())
                    }
                    return newValue
                }
            }
        ) { if (CUSTOM_PROPERTY_KEY == it) DEFAULT_CUSTOM_PROPERTY_VALUE else null }

        converters.add(0, valueConverter!!) // to enable to get inner property values
    }

    @Test
    fun testSimpleParameterWithNoValue() {
        Assertions.assertEquals("", valueConverter!!.convert("\${1}"))
    }

    @Test
    fun testSimpleParameterWithNoValueAndDefault() {
        Assertions.assertEquals("def", valueConverter!!.convert("\${1:def}"))
    }

    @Test
    fun testSimpleParameterWithNoValueAndDefaultIsNull() {
        Assertions.assertNull(valueConverter!!.convert("\${1:null}"))
    }

    @Test
    fun testWithNestedParametersAndDefault() {
        Assertions.assertEquals("v3", valueConverter!!.convert("\${key_1 : \${key_2 : v3}}"))
    }

    @Test
    fun testWithNestedParametersAndDefaultWhenFirstIsEmpty() {
        whenever(testScenarioState!!.get("id")).thenReturn("")
        Assertions.assertEquals("v3", valueConverter!!.convert("\${id : v3}"))
    }

    @Test
    fun testWithComplexNestedParametersAndAttachedText() {
        Assertions.assertEquals("id:v3_end", valueConverter!!.convert("\${key_1 : id:\${key_2 : \${id:v3}}_end}"))
    }

    @Test
    fun testWithNestedFunctionsWithAttachedText() {
        whenever(testScenarioState!!.get("id")).thenReturn("10")
        Assertions.assertEquals(
            "_10_-$DEFAULT_CUSTOM_PROPERTY_VALUE",
            valueConverter!!.convert("\${key_1 : _get(id)_-\${$CUSTOM_PROPERTY_KEY}}")
        )
    }

    @Test
    fun testWithNestedFunctionReturningObject() {
        Assertions.assertEquals(LocalDateTime::class.java, valueConverter!!.convert("\${key_1 : now(UTC)}")!!.javaClass)
    }

    @Test
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