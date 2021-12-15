package org.skellig.teststep.processing.model.factory

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.exception.TestValueConversionException

class PropertyParserTest {
    companion object {
        private const val CUSTOM_PROPERTY_KEY = "custom.properties_1"
        private const val CUSTOM_PROPERTY_KEY_2 = "custom.properties_2"
        private const val KEY_ONE = "1"
        private const val KEY_TWO = "2"
        private const val KEY_REF = "a-b-1"
        private const val DEFAULT_CUSTOM_PROPERTY_VALUE = "from custom properties"
        private const val DEFAULT_CUSTOM_PROPERTY_VALUE_2 = "from custom properties 2"
    }

    private var propertyParser: PropertyParser? = null

    @BeforeEach
    fun setUp() {

        propertyParser = PropertyParser { when(it) {
            CUSTOM_PROPERTY_KEY -> DEFAULT_CUSTOM_PROPERTY_VALUE
            CUSTOM_PROPERTY_KEY_2 -> DEFAULT_CUSTOM_PROPERTY_VALUE_2
            KEY_REF -> CUSTOM_PROPERTY_KEY
            KEY_ONE -> "a"
            KEY_TWO -> "b"
            else -> null
        } }
    }

    @Test
    fun testSimpleParameterWithNoValue() {
        Assertions.assertThrows(TestValueConversionException::class.java) {
            propertyParser!!.parse("\${a}", emptyMap())
        }
    }

    @Test
    fun testExistingParameterButWithExtraSpaceAfter() {
        Assertions.assertThrows(TestValueConversionException::class.java) {
            propertyParser!!.parse("\${1  }", emptyMap())
        }
    }

    @Test
    fun testExistingParameterButWithExtraSpaceBefore() {
        Assertions.assertThrows(TestValueConversionException::class.java) {
            propertyParser!!.parse("\${  1}", emptyMap())
        }
    }

    @Test
    fun testSimpleParameterWithNoValueAndDefault() {
        assertEquals("def", propertyParser!!.parse("\${a:def}", emptyMap()))
    }

    @Test
    fun testTwoParametersMerged() {
        assertEquals("ab", propertyParser!!.parse("\${1}\${2}", emptyMap()))
    }

    @Test
    fun testSimpleParameterWithNoValueAndDefaultIsNull() {
        assertNull(propertyParser!!.parse("\${a:null}", emptyMap()))
    }

    @Test
    fun testSimpleParameterWithNoValueAndDefaultIsEmpty() {
        assertEquals("", propertyParser!!.parse("\${a:}", emptyMap()))
    }

    @Test
    fun testParameterWithWrappedSpecialCharacter() {
        assertEquals("'\${b} - {}' {}", propertyParser!!.parse("\${a:'\${b} - {}'} \\{\\}", emptyMap()))
    }

    @Test
    fun testNoParameterWithSpecialCharacter() {
        assertEquals("([\\w]{10})", propertyParser!!.parse("([\\w]\\{10\\})", emptyMap()))
    }

    @Test
    fun testConvertNull() {
        assertNull(propertyParser!!.parse(null, emptyMap()))
    }

    @Test
    fun testWithNestedParametersAndDefault() {
        assertEquals("v3", propertyParser!!.parse("\${key_1 : \${key_2 : v3}}", emptyMap()))
    }

    @Test
    fun testWithParameterAsObjectAndNoAttachedText() {
        val value = listOf("a", "b")
        assertEquals(value, propertyParser!!.parse("\${key_1}", mapOf(Pair("key_1", value))))
    }

    @Test
    fun testWithParameterAsObjectAndAttachedText() {
        val value = listOf("a", "b")
        assertEquals("_${value}_", propertyParser!!.parse("_\${key_1}_", mapOf(Pair("key_1", value))))
    }

    @Test
    fun testWithParameterAsObjectAsDefaultValue() {
        val value = listOf("a", "b")
        assertEquals(value, propertyParser!!.parse("\${key_n: \${key_1}}", mapOf(Pair("key_1", value))))
    }

    @Test
    fun testWithComplexNestedParametersAndAttachedText() {
        assertEquals("id:v3_end", propertyParser!!.parse("\${key_1 : id:\${key_2 : \${id:v3}_end}}", emptyMap()))
    }

    @Test
    fun testWithSpecialCharsAsValue() {
        assertEquals("{a} - \${:)}} $1 \\", propertyParser!!.parse("\\{\${$KEY_ONE}\\} - $\\{\\:)\\}\\} $1 \\", emptyMap()))
    }

    @Test
    fun testWithComplexNestedParametersAndAttachedText2() {
        assertEquals("prefix_" +
                " __b" + // only 1 space is removed after ':'
                " __ " + // preserve spaces after '}' and before '${'
                "a__" + // only 1 spaces is removed for key '1', before '}'
                " / get(a) / v1 / " + // all spaces must be preserved as nothing is inside '{ }'
                " ",  // 1 space is removed after ':' and another space removed before '}'
            propertyParser!!.parse("prefix_\${a :  __\${ $KEY_TWO : 10 } __ \${$KEY_ONE }__} / get(\${$KEY_ONE}) / v1 / \${ b:   }", emptyMap()))
    }

    @Test
    fun testWithNestedFunctionsWithAttachedText() {
        assertEquals(
            "_#[get(a).f1.size]_-$DEFAULT_CUSTOM_PROPERTY_VALUE",
            propertyParser!!.parse("\${key_1 : _#[get(\${$KEY_ONE}).f1.size]_-\${$CUSTOM_PROPERTY_KEY}}", emptyMap())
        )
    }

    @Test
    fun testWithManyParametersAndAttachedText() {
        assertEquals(
            "/$DEFAULT_CUSTOM_PROPERTY_VALUE/$DEFAULT_CUSTOM_PROPERTY_VALUE_2/get",
            propertyParser!!.parse("/\${$CUSTOM_PROPERTY_KEY:default}/\${$CUSTOM_PROPERTY_KEY_2}/get", emptyMap()))
    }


    @Test
    fun testWithCustomProperties() {
        assertEquals(
            "$DEFAULT_CUSTOM_PROPERTY_VALUE - $DEFAULT_CUSTOM_PROPERTY_VALUE",
            propertyParser!!.parse("\${$CUSTOM_PROPERTY_KEY} - \${$CUSTOM_PROPERTY_KEY}", emptyMap())
        )
    }

    @Test
    fun testWithSystemProperties() {
        val key = "key1"
        System.setProperty(key, "v1")

        assertEquals(System.getProperty(key), propertyParser!!.parse("\${$key}", emptyMap()))
    }
}