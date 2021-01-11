package org.skellig.task.async

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skellig.task.async.AsyncTaskUtils.Companion.runCallableAsync
import org.skellig.task.async.AsyncTaskUtils.Companion.runTaskAsync
import org.skellig.task.async.AsyncTaskUtils.Companion.shutdown
import org.skellig.task.exception.TaskRunException
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

@DisplayName("Run async task")
class AsyncTaskUtilsTest {

    companion object {
        private val RESPONSE = "ok"

        @AfterAll
        fun afterAll() {
            shutdown()
        }
    }

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
        val asyncResult = runTaskAsync ({
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