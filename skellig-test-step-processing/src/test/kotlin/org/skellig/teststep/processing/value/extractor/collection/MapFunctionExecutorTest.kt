package org.skellig.teststep.processing.value.extractor.collection

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MapFunctionExecutorTest {

    private val mapFunctionExecutor = MapFunctionExecutor()

    @Test
    fun testCount() {
        assertEquals(listOf("1", "2", "3"), mapFunctionExecutor.extractFrom("count", listOf(1, 2, 3), arrayOf({ v: Any? -> v.toString() })))
    }

}