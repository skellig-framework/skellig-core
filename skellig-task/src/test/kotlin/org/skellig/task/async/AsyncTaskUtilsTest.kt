package org.skellig.task.async

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.skellig.task.async.AsyncTaskUtils.Companion.runCallableAsync
import org.skellig.task.async.AsyncTaskUtils.Companion.runTaskAsync
import org.skellig.task.async.AsyncTaskUtils.Companion.runTasksAsyncAndWait
import org.skellig.task.async.AsyncTaskUtils.Companion.shutdown
import org.skellig.task.exception.TaskRunException
import java.util.*
import java.util.concurrent.*

class AsyncTaskUtilsTest {

    companion object {
        private const val RESPONSE = "ok"

        @AfterAll
        @JvmStatic
        fun afterAll() {
            shutdown()
        }
    }

    @Nested
    @DisplayName("Run async task")
    internal inner class RunTask {
        @Test
        @DisplayName("When result received within a timeout")
        @Throws(Exception::class)
        fun testAsyncTask() {
            val asyncResult: Future<String> = runCallableAsync {
                Thread.sleep(50)
                RESPONSE
            }
            assertEquals(RESPONSE, asyncResult[500, TimeUnit.MILLISECONDS])
        }

        @Test
        @DisplayName("When result received within a timeout And stop condition satisfied")
        @Throws(Exception::class)
        fun testAsyncTaskWithStopCondition() {
            val countDownLatch = CountDownLatch(6)
            val asyncResult = runTaskAsync({
                countDownLatch.countDown()
                if (countDownLatch.count > 1) {
                    return@runTaskAsync null
                } else {
                    countDownLatch.countDown()
                    return@runTaskAsync RESPONSE
                }
            }, { obj: String? -> Objects.nonNull(obj) }, 10, 1000)

            countDownLatch.await(1000, TimeUnit.MILLISECONDS)
            assertEquals(0, countDownLatch.count)
            assertEquals(RESPONSE, asyncResult[10, TimeUnit.MILLISECONDS])
        }

        @Test
        @DisplayName("When getting result from async operation And inner exception is thrown")
        fun testAsyncTaskWhenThrowException() {
            val asyncResult: Future<String> = runCallableAsync {
                Thread.sleep(100)
                throw TaskRunException("oops")
            }

            val ex = Assertions.assertThrows(ExecutionException::class.java) { asyncResult[1000, TimeUnit.MILLISECONDS] }
            assertEquals("org.skellig.task.exception.TaskRunException: oops", ex.message)
        }

        @Test
        @DisplayName("When getting result from async operation not received within timeout")
        fun testAsyncTaskWhenNotReceivedWithinTimeout() {
            val asyncResult: Future<String> = runCallableAsync {
                Thread.sleep(200)
                "result"
            }

            Assertions.assertThrows(TimeoutException::class.java) { asyncResult[10, TimeUnit.MILLISECONDS] }
        }

        @Nested
        @DisplayName("When one task submitted And wait for the result")
        internal inner class RunTaskAndWait {
            @Test
            @DisplayName("Then task run synchronously")
            fun testAsyncTaskAndGetResult() {
                val asyncResult = runTasksAsyncAndWait(mapOf(Pair("t1", { "r1" })), { true }, 0, 0, 500)

                assertEquals("r1", asyncResult["t1"])
            }

            @Test
            @DisplayName("And condition not satisfied Then check latest non-null result returned")
            fun testAsyncTaskWhenConditionNotSatisfied() {
                var counter = 0
                val asyncResult = runTasksAsyncAndWait(
                    mapOf(Pair("t1") { if (counter++ == 1) "r1" else null }),
                    { false }, 1, 5, 500
                )

                assertEquals("r1", asyncResult["t1"])
            }
        }
    }

    @Nested
    @DisplayName("Run async tasks")
    internal inner class RunTasks {

        @Test
        @DisplayName("When many tasks And results received within a timeout")
        fun testAsyncTasks() {
            val asyncResult = runTasksAsyncAndWait(createMapOfTasks(150, 50), { it["t1"] != null && it["t2"] != null }, 0, 0, 500)

            assertEquals("r1", asyncResult["t1"])
            assertEquals("r2", asyncResult["t2"])
        }

        @Test
        @DisplayName("When many tasks And one result not received within a timeout")
        fun testAsyncTasksWhenOneTimesOut() {
            val asyncResult = runTasksAsyncAndWait(createMapOfTasks(3000, 50), { it["t1"] != null }, 0, 0, 200)

            Assertions.assertNull(asyncResult["t1"])
            assertEquals("r2", asyncResult["t2"])
        }

        @Test
        @DisplayName("When many tasks And timeout is 0 Then wait until received")
        fun testAsyncTasksWhenTimeoutZero() {
            val asyncResult = runTasksAsyncAndWait(createMapOfTasks(120, 20), { it["t1"] != null && it["t2"] != null }, 0, 0, 0)

            assertEquals("r1", asyncResult["t1"])
            assertEquals("r2", asyncResult["t2"])
        }

        @Test
        @DisplayName("When many tasks And retries 3 times And results received within a timeout")
        fun testAsyncTasksWithRetries() {
            val asyncResult = runTasksAsyncAndWait(createMapOfTasks(150, 50, 3), { it["t1"] != null }, 0, 3, 0)

            assertEquals("r1", asyncResult["t1"])
            assertEquals("r2", asyncResult["t2"])
        }

        @Test
        @DisplayName("When many tasks And retries 2 times And one result not received after retries")
        fun testAsyncTasksWhenOneNotReceivedAfterRetries() {
            val asyncResult = runTasksAsyncAndWait(createMapOfTasks(150, 50, 3), { it["t1"] != null }, 0, 2, 0)

            Assertions.assertNull(asyncResult["t1"])
            assertEquals("r2", asyncResult["t2"])
        }

        private fun createMapOfTasks(taskOneDelay: Long, taskTwoDelay: Long, taskOneMaxRetry: Int = 0): Map<String, () -> String?> {
            var inc = 0
            return mapOf(Pair(
                "t1"
            ) {
                if (taskOneMaxRetry > 0) {
                    // this case is triggered when testing a task with retries
                    Thread.sleep(10)
                    if (++inc >= taskOneMaxRetry) "r1" else null
                } else {
                    Thread.sleep(taskOneDelay)
                    "r1"
                }
            },
                Pair("t2") {
                    Thread.sleep(taskTwoDelay)
                    "r2"
                }
            )
        }
    }
}