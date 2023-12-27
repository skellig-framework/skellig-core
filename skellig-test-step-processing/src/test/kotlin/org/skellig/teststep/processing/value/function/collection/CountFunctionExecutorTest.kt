package org.skellig.teststep.processing.value.function.collection

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class CountFunctionExecutorTest {

    private val countFunctionExecutor = CountFunctionExecutor()

    @Test
    fun testCount() {
        assertEquals(4, countFunctionExecutor.execute("count", listOf(1, 0, 3, 7, 0, 16), arrayOf({ v: Any? -> v as Int > 0 })))
    }

}