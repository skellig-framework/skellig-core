package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SubStringLastFunctionExecutorTest {

    private val functionExecutor = SubStringLastFunctionExecutor()

    @Test
    fun testSubstringLast() {
        assertEquals("c", functionExecutor.execute("subString", "a/b/c", arrayOf("/")))
    }
}