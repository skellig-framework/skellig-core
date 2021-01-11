package org.skellig.task

import java.util.concurrent.Callable
import java.util.function.Predicate

class TaskUtils {

    companion object {

        @JvmStatic
        @Throws(TaskRunException::class)
        fun <T> runTask(task: Callable<T>, stopCondition: Predicate<T>, delay: Int, timeout: Int): T {
            return runTaskUntil(task, stopCondition, delay, timeout)
        }

        @JvmStatic
        @Throws(TaskRunException::class)
        private fun <T> runTaskUntil(task: Callable<T>, stopCondition: Predicate<T>, delay: Int, timeout: Int): T {
            var newTimeout: Int
            var result: T
            do {
                val startTime = System.currentTimeMillis()
                result = try {
                    task.call()
                } catch (ex: Exception) {
                    throw TaskRunException(ex.message, ex)
                }
                val totalExecutionTime = (System.currentTimeMillis() - startTime).toInt()
                newTimeout = if (stopCondition.test(result)) {
                    0
                } else {
                    if (delay > 0) {
                        delay(delay)
                    }
                    timeout - delay - totalExecutionTime
                }
            } while (newTimeout > 0)

            return result
        }

        private fun delay(idleMilliseconds: Int) {
            try {
                Thread.sleep(idleMilliseconds.toLong())
            } catch (ignored: InterruptedException) {
            }
        }
    }
}