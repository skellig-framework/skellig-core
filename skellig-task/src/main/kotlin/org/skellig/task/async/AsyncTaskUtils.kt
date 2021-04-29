package org.skellig.task.async

import org.skellig.task.TaskUtils.Companion.runTask
import org.skellig.task.exception.TaskRunException
import java.util.concurrent.*
import java.util.function.Predicate

class AsyncTaskUtils {

    companion object {
        private val executorService = Executors.newCachedThreadPool()

        @JvmStatic
        fun runTaskAsync(task: Runnable) {
            try {
                executorService.submit(task)
            } catch (e: Exception) {
                throw TaskRunException(e.message, e)
            }
        }

        @JvmStatic
        fun <T> runTaskAsync(task: () -> T): Future<T> {
            return runCallableAsync { runTask(task, 0, 0, { true }) }
        }

        @JvmStatic
        fun <T> runCallableAsync(task: () -> T): Future<T> {
            return try {
                executorService.submit(task)
            } catch (e: Exception) {
                throw TaskRunException(e.message, e)
            }
        }

        @JvmStatic
        fun <T> runTasksAsyncAndWait(tasks: Map<*, () -> T>, stopCondition: (Map<*, T?>) -> Boolean = { true },
                                     delay: Int = 0, attempts: Int = 0, timeout: Int = 0): Map<*, T?> =
                runTasksAsyncAndWaitInternal(tasks, stopCondition, delay, attempts, timeout, mapOf<Any, T?>())

        @JvmStatic
        fun <T> runTaskAsync(task: Callable<T>, stopCondition: Predicate<T>, timeout: Int): Future<T> {
            return runCallableAsync { runTask(task, stopCondition, 0, timeout) }
        }

        @JvmStatic
        fun <T> runTaskAsync(task: Callable<T>, stopCondition: Predicate<T>, delay: Int, timeout: Int): Future<T> {
            return runCallableAsync { runTask(task, stopCondition, delay, timeout) }
        }

        @JvmStatic
        fun <T> runTaskAsync(task: Callable<T>, delay: Int, attempts: Int, stopCondition: Predicate<T>): Future<T> {
            return runCallableAsync { runTask(task, delay, attempts, stopCondition) }
        }

        @JvmStatic
        fun shutdown() {
            executorService.shutdown()
            try {
                executorService.awaitTermination(30, TimeUnit.SECONDS)
            } catch (e: InterruptedException) {
                throw TaskRunException("Waiting for termination of the running async tasks took too long", e)
            }
        }

        private fun <T> runTasksAsyncAndWaitInternal(tasks: Map<*, () -> T>,
                                                     stopCondition: (Map<*, T?>) -> Boolean = { true },
                                                     delay: Int = 0, attempts: Int = 0, timeout: Int = 0,
                                                     previousResults: Map<*, T?>): Map<*, T?> {
            val newAttempt = attempts - 1
            val futures = tasks.map { it.key to runTaskAsync(it.value) }.toMap()
            val finalResult = futures.map {
                // if result is null then try to get non-null value from previous result if exists
                it.key to (waitAndGetResult(it.value, timeout) ?: previousResults[it.key])
            }.toMap()
            // collect all results from tasks and apply stopCondition
            return if (newAttempt <= 0 || stopCondition(finalResult)) finalResult
            else {
                Thread.sleep(delay.toLong())
                runTasksAsyncAndWaitInternal(tasks, stopCondition, delay, newAttempt, timeout, finalResult)
            }
        }

        private fun <T> waitAndGetResult(taskResult: Future<T>, timeout: Int) =
                try {
                    if (timeout > 0) taskResult[timeout.toLong(), TimeUnit.MILLISECONDS]
                    else taskResult[1, TimeUnit.MINUTES]
                } catch (ex: Exception) {
                    taskResult.cancel(true)
                    when (ex) {
                        is InterruptedException, is TimeoutException -> null
                        // in case if exception comes from the task then throw it
                        else -> ex.cause?.let { throw it } ?: throw ex
                    }
                }
    }
}