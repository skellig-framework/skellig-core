package org.skellig.task.async

import org.junit.jupiter.api.*
import org.skellig.task.async.AsyncTaskUtils.Companion.runCallableAsync
import org.skellig.task.async.AsyncTaskUtils.Companion.runTaskAsync
import org.skellig.task.async.AsyncTaskUtils.Companion.runTasksAsyncAndGet
import org.skellig.task.async.AsyncTaskUtils.Companion.shutdown
import org.skellig.task.exception.TaskRunException
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

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
            Assertions.assertEquals(RESPONSE, asyncResult[500, TimeUnit.MILLISECONDS])
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
            Assertions.assertEquals(0, countDownLatch.count)
            Assertions.assertEquals(RESPONSE, asyncResult[10, TimeUnit.MILLISECONDS])
        }

        @Test
        @DisplayName("When getting result from async operation not received within timeout")
        fun testAsyncTaskWhenThrowException() {
            val asyncResult: Future<String> = runCallableAsync {
                Thread.sleep(100)
                throw TaskRunException("oops")
            }

            Assertions.assertThrows(ExecutionException::class.java) { asyncResult[300, TimeUnit.MILLISECONDS] }
        }

    }

    @Nested
    @DisplayName("Run async tasks")
    internal inner class RunTasks {

        @Test
        @DisplayName("When many tasks And results received within a timeout")
        @Throws(Exception::class)
        fun testAsyncTasks() {
            val asyncResult = runTasksAsyncAndGet(createMapOfTasks(150, 50), { it != null }, 0, 500)

            Assertions.assertEquals("r1", asyncResult["t1"])
            Assertions.assertEquals("r2", asyncResult["t2"])
        }

        @Test
        @DisplayName("When many tasks And one result not received within a timeout")
        @Throws(Exception::class)
        fun testAsyncTasksWhenOneTimesOut() {
            val asyncResult = runTasksAsyncAndGet(createMapOfTasks(3000, 50), { it != null }, 0, 200)

            Assertions.assertNull(asyncResult["t1"])
            Assertions.assertEquals("r2", asyncResult["t2"])
        }

        @Test
        @DisplayName("When many tasks And timeout is 0 Then wait until received")
        @Throws(Exception::class)
        fun testAsyncTasksWhenTimeoutZero() {
            val asyncResult = runTasksAsyncAndGet(createMapOfTasks(120, 20), { it != null }, 0, 0)

            Assertions.assertEquals("r1", asyncResult["t1"])
            Assertions.assertEquals("r2", asyncResult["t2"])
        }

        private fun createMapOfTasks(taskOneDelay: Long, taskTwoDelay: Long) = mapOf(Pair("t1",
                {
                    Thread.sleep(taskOneDelay)
                    "r1"
                }),
                Pair("t2", {
                    Thread.sleep(taskTwoDelay)
                    "r2"
                })
        )
    }
}