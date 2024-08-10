package org.skellig.teststep.processing.util

import org.skellig.teststep.processing.exception.TaskRunException
import java.util.concurrent.Callable
import java.util.function.Predicate

object TaskUtils {

    @Throws(TaskRunException::class)
    fun <T> runTask(task: Callable<T?>, stopCondition: Predicate<T?>, delay: Int, timeout: Int): T? {
        return runTaskUntil(task, stopCondition, delay, timeout)
    }

    @Throws(TaskRunException::class)
    fun <T> runTask(task: Callable<T?>, delay: Int, attempts: Int, stopCondition: Predicate<T?>): T? {
        return runTaskUntil(task, delay, attempts, stopCondition)
    }

    @Throws(TaskRunException::class)
    private fun <T> runTaskUntil(task: Callable<T?>, stopCondition: Predicate<T?>, delay: Int, timeout: Int): T? {
        var newTimeout: Int = timeout
        var result: T?
        do {
            val startTime = System.currentTimeMillis()
            result = call(task)
            val totalExecutionTime = (System.currentTimeMillis() - startTime).toInt()
            newTimeout = if (stopCondition.test(result)) {
                0
            } else {
                if (delay > 0) {
                    delay(delay)
                }
                newTimeout - delay - totalExecutionTime
            }
        } while (newTimeout > 0)

        return result
    }

    @Throws(TaskRunException::class)
    private fun <T> runTaskUntil(task: Callable<T?>, delay: Int, attempts: Int, stopCondition: Predicate<T?>): T? {
        var newAttempts = attempts
        var result: T?
        do {
            result = call(task)
            newAttempts = if (stopCondition.test(result)) {
                0
            } else {
                if (delay > 0) {
                    delay(delay)
                }
                newAttempts - 1
            }
        } while (newAttempts > 0)

        return result
    }

    private fun <T> call(task: Callable<T?>): T? =
        try {
            task.call()
        } catch (ex: Exception) {
            throw TaskRunException(ex.message, ex)
        }


    private fun delay(idleMilliseconds: Int) {
        try {
            Thread.sleep(idleMilliseconds.toLong())
        } catch (ignored: InterruptedException) {
        }
    }
}