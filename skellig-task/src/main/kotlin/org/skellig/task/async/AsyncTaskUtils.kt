package org.skellig.task.async

import org.skellig.task.TaskUtils.Companion.runTask
import org.skellig.task.exception.TaskRunException
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
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
        fun <T> runCallableAsync(task: () -> T): Future<T> {
            return try {
                executorService.submit(task)
            } catch (e: Exception) {
                throw TaskRunException(e.message, e)
            }
        }

        @JvmStatic
        fun <T> runTasksAsyncAndGet(tasks: Map<*, () -> T>, stopCondition: Predicate<T>, delay: Int, timeout: Int): Map<*, T?> {
            val futures = tasks.map { it.key to runTaskAsync(it.value, stopCondition, delay, timeout) }.toMap()
            return futures
                    .map {
                        it.key to
                                try {
                                    if(timeout > 0) it.value[timeout.toLong(), TimeUnit.MILLISECONDS]
                                    else it.value[1, TimeUnit.MINUTES]
                                } catch (ex: Exception) {
                                    it.value.cancel(true)
                                    null
                                }
                    }
                    .toMap()
        }

        @JvmStatic
        fun <T> runTaskAsync(task: Callable<T>, stopCondition: Predicate<T>, timeout: Int): Future<T> {
            return runCallableAsync { runTask(task, stopCondition, 0, timeout) }
        }

        @JvmStatic
        fun <T> runTaskAsync(task: Callable<T>, stopCondition: Predicate<T>, delay: Int, timeout: Int): Future<T> {
            return runCallableAsync { runTask(task, stopCondition, delay, timeout) }
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
    }
}