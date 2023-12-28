package org.skellig.teststep.processing.value.function.collection

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GroupFunctionExecutorTest {

    private val groupFunctionExecutor = GroupByFunctionExecutor()

    @Test
    fun testCount() {
        assertEquals(
            mapOf(
                Pair(12, listOf(12)),
                Pair(0, listOf(0, 0)),
                Pair(3, listOf(3)),
                Pair(5, listOf(5, 5, 5))
            ),
            groupFunctionExecutor.execute("count", listOf(12, 0, 3, 5, 0, 5, 5), arrayOf({ v: Any? -> v }))
        )
    }

}