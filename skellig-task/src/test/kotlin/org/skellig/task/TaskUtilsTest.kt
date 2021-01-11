package org.skellig.task

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skellig.task.TaskUtils.Companion.runTask
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@DisplayName("Run task")
class TaskUtilsTest {

    private val RESPONSE = "ok"

    @Test
    @DisplayName("When result received within a timeout And stop condition satisfied")
    fun testRunTaskWithStopCondition() {
        val atomicInteger = AtomicInteger(0)
        val response = runTask({
            if (atomicInteger.incrementAndGet() < 7) {
                return@runTask null
            } else {
                return@runTask RESPONSE
            }
        }, { obj: String? -> Objects.nonNull(obj) }, 10, 1000)
        Assertions.assertEquals(7, atomicInteger.get())
        Assertions.assertEquals(RESPONSE, response)
    }

    @Test
    @DisplayName("When result received within a timeout And stop condition satisfied")
    fun testRunTaskWithStopConditionWhenTimedOut() {
        Assertions.assertNull(runTask<String?>({ null }, { obj: String? -> Objects.nonNull(obj) }, 100, 1))
    }
}