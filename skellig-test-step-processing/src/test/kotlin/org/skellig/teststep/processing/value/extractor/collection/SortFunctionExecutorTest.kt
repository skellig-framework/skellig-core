package org.skellig.teststep.processing.value.extractor.collection

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SortFunctionExecutorTest {

    private val functionExecutor = SortFunctionExecutor()

    @Test
    fun testSort() {
        assertEquals(listOf(0, 3, 4, 7, 9, 12), functionExecutor.extractFrom("", listOf(4, 3, 12, 0, 9, 7), arrayOf({ v: Any? -> v as Int })))
        assertEquals(
            listOf(mapOf(Pair("price", 0)), mapOf(Pair("price", 3)), mapOf(Pair("price", 9))),
            functionExecutor.extractFrom("", listOf(mapOf(Pair("price", 9)), mapOf(Pair("price", 3)), mapOf(Pair("price", 0))), arrayOf({ v: Any? -> (v as Map<*, *>)["price"] }))
        )
    }

}