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
    @DisplayName("When result received within a timeout And stop condition satisfied Then return result")
    fun testRunTaskWithStopCondition() {
        val atomicInteger = AtomicInteger(0)
        val response = runTask({
            if (atomicInteger.incrementAndGet() < 7) {
                return@runTask null
            } else {
                return@runTask RESPONSE
            }
        }, { it != null }, 10, 1000)

        Assertions.assertEquals(7, atomicInteger.get())
        Assertions.assertEquals(RESPONSE, response)
    }

    @Test
    @DisplayName("When result not received within a timeout And stop condition not satisfied Then return null")
    fun testRunTaskWithStopConditionWhenTimedOut() {
        Assertions.assertNull(runTask<String?>({ null }, { it != null }, 100, 200))
    }

    @Test
    @DisplayName("When retry getting result several times And stop condition satisfied Then return result")
    fun testRunTaskWithStopConditionAndRetryTimes() {
        val atomicInteger = AtomicInteger(0)
        val response = runTask({
            if (atomicInteger.incrementAndGet() < 5) {
                return@runTask null
            } else {
                return@runTask RESPONSE
            }
        }, 10, 6, { it != null })

        Assertions.assertEquals(5, atomicInteger.get())
        Assertions.assertEquals(RESPONSE, response)
    }

    @Test
    @DisplayName("When result not received after several retries And stop not condition satisfied Then return null")
    fun testRunTaskWithStopConditionWhenRetriesExpired() {
        Assertions.assertNull(runTask<String?>({ null }, 100, 2, { it != null }))
    }
}