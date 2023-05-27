package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.value.function.exception.FunctionValueExecutionException

class RandomStringFunctionExecutorTest {

    private val functionExecutor = RandomStringFunctionExecutor()

    @Test
    fun testRandomStringWithValidArg() {
        assertEquals(3, functionExecutor.execute("randString", arrayOf("3")).toString().length)
        assertEquals(14, functionExecutor.execute("randString", arrayOf("14")).toString().length)
        assertEquals(0, functionExecutor.execute("randString", arrayOf("0")).toString().length)
    }

    @Test
    fun testRandomStringWithInvalidArg() {
        assertThrows(FunctionValueExecutionException::class.java) {
            functionExecutor.execute(
                "randString",
                arrayOf("invalid")
            ).toString().length
        }
    }
}