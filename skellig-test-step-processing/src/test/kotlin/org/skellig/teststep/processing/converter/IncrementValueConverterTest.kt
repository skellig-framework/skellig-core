package org.skellig.teststep.processing.converter

import org.junit.jupiter.api.*
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

class IncrementValueConverterTest {

    private var converter: IncrementValueConverter? = null

    @BeforeEach
    fun setUp() {
        converter = IncrementValueConverter()
    }

    @AfterEach
    @Throws(IOException::class)
    fun cleanUp() {
        Files.delete(Paths.get("skellig-inc.tmp"))
    }

    @Test
    @DisplayName("Increment value first time Then verify it returned default value with replaced zeros")
    fun testIncrementSimpleValueFirstTime() {
        val result = converter!!.convert("inc(id,10)")

        Assertions.assertEquals("0000000001", result)
    }

    @Test
    @DisplayName("Increment value second time Then verify it returned incremented value by 1")
    fun testIncrementSimpleValueSecondTime() {
        val value = "inc(id,10)"

        converter!!.convert(value)

        val result = converter!!.convert(value)
        Assertions.assertEquals("0000000002", result)
    }

    @Test
    @DisplayName("Increment value several times Then verify it returned correct incremented value")
    fun testIncrementSimpleValueSeveralTimes() {
        val value = "inc(id,3)"
        var result: Any? = null
        for (i in 0..9) {
            result = converter!!.convert(value)
        }
        Assertions.assertEquals("010", result)
    }

    @Test
    @DisplayName("Increment value more times than defined in parameter Then verify it does not overflow")
    fun testIncrementValueMoreThanLengthOfRegex() {
        val value = "inc(id,1)"
        var result: Any? = null
        for (i in 0..9) {
            result = converter!!.convert(value)
        }
        Assertions.assertEquals("9", result)
    }

    @Test
    @DisplayName("Increment value with different limits several times Then verify it does not overflow and increment where possible")
    fun testIncrementValueWithDifferentLimits() {
        val value = "inc(id ,4)"
        val valueWithId = "inc(id)"

        converter!!.convert(value)

        val result = converter!!.convert(valueWithId)

        Assertions.assertEquals("0002", result)
    }

    @Test
    @DisplayName("Increment different values Then verify it takes correct values from file and increment them")
    fun testIncrementDifferentValues() {
        val value1 = "inc(id, 5)"
        val value2 = "inc(3)"
        val value3 = "inc()"
        var result1: Any? = null
        var result2: Any? = null
        var result3: Any? = null
        for (i in 0..4) {
            val converter = IncrementValueConverter()
            result1 = converter.convert(value1)
            result2 = converter.convert(value2)
            result3 = converter.convert(value3)
        }

        Assertions.assertEquals("00005", result1)
        Assertions.assertEquals("005", result2)
        Assertions.assertEquals("5", result3)
    }
}