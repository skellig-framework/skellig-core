package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.*
import org.skellig.teststep.processing.value.function.IncrementFunctionExecutor
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

class IncrementFunctionExecutorTest {

    private var converter: IncrementFunctionExecutor? = null

    @BeforeEach
    fun setUp() {
        converter = IncrementFunctionExecutor()
    }

    @AfterEach
    @Throws(IOException::class)
    fun cleanUp() {
        Files.delete(Paths.get("skellig-inc.tmp"))
    }

    @Test
    @DisplayName("Increment value first time Then verify it returned default value with replaced zeros")
    fun testIncrementSimpleValueFirstTime() {
        val result = converter!!.execute("inc", null, arrayOf("id","10"))

        Assertions.assertEquals("0000000001", result)
    }

    @Test
    @DisplayName("Increment value second time Then verify it returned incremented value by 1")
    fun testIncrementSimpleValueSecondTime() {
        val args: Array<Any?> = arrayOf("id", "10")
        converter!!.execute("inc", null, args)

        val result = converter!!.execute("inc", null, args)

        Assertions.assertEquals("0000000002", result)
    }

    @Test
    @DisplayName("Increment value several times Then verify it returned correct incremented value")
    fun testIncrementSimpleValueSeveralTimes() {
        var result: Any? = null
        for (i in 0..9) {
            result = converter!!.execute("inc", null, arrayOf("id","3"))
        }
        Assertions.assertEquals("010", result)
    }

    @Test
    @DisplayName("Increment value more times than defined in parameter Then verify it does not overflow")
    fun testIncrementValueMoreThanLengthOfRegex() {
        var result: Any? = null
        for (i in 0..9) {
            result = converter!!.execute("inc", null, arrayOf("id","1"))
        }
        Assertions.assertEquals("9", result)
    }

    @Test
    @DisplayName("Increment value with different limits several times Then verify it does not overflow and increment where possible")
    fun testIncrementValueWithDifferentLimits() {
        converter!!.execute("inc", null, arrayOf("id","4"))

        val result = converter!!.execute("inc", null, arrayOf("id"))

        Assertions.assertEquals("0002", result)
    }

    @Test
    @DisplayName("Increment different values Then verify it takes correct values from file and increment them")
    fun testIncrementDifferentValues() {
        var result1: Any? = null
        var result2: Any? = null
        var result3: Any? = null
        for (i in 0..4) {
            val converter = IncrementFunctionExecutor()
            result1 = converter.execute("inc", null, arrayOf("id","5"))
            result2 = converter.execute("inc", null, arrayOf("3"))
            result3 = converter.execute("inc", null, emptyArray())
        }

        Assertions.assertEquals("00005", result1)
        Assertions.assertEquals("005", result2)
        Assertions.assertEquals("5", result3)
    }
}