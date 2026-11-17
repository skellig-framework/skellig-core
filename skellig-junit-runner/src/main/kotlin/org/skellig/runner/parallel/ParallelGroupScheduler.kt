package org.skellig.runner.parallel

import org.junit.runners.model.RunnerScheduler
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * Runs the work of parallel groups, either one after another on the calling thread, or on a fixed pool of worker
 * threads named `skellig-parallel-<n>`. A parallel group is one piece of work, so its scenarios run in order on the
 * thread that runs it.
 *
 * [finished] waits for all the work, and then throws the first exception which the work threw, as nothing in the
 * work is expected to throw. The first one is the one of the work that was scheduled first, and the exceptions
 * of the rest of the work are added to it as suppressed.
 *
 * Work which runs on the scheduler can hand its own pieces of work to the same worker threads with [scheduleAndWait].
 */
class ParallelGroupScheduler private constructor(private val executor: ExecutorService?) : RunnerScheduler {

    private val futures = mutableListOf<Future<*>>()

    override fun schedule(childStatement: Runnable) {
        futures.add(submit(childStatement))
    }

    /**
     * Runs the [work] on the worker threads and waits for all of it, then throws the first exception which it threw,
     * the same way as [finished] does.
     *
     * It is for work which itself runs on a worker thread of this scheduler. Such a caller keeps its thread while it
     * waits, so it runs the pieces which no worker thread has started yet. Otherwise, the callers could take all the
     * worker threads and wait forever for work that has no thread left to run on.
     */
    fun scheduleAndWait(work: List<Runnable>) {
        val tasks = work.map { submit(it) }
        try {
            tasks.forEach { it.run() }
            await(tasks)
        } catch (ex: InterruptedException) {
            tasks.forEach { it.cancel(true) }
            throw ex
        }
    }

    override fun finished() {
        try {
            await(futures)
        } catch (ex: InterruptedException) {
            executor?.shutdownNow()
            throw ex
        } finally {
            executor?.shutdown()
        }
    }

    private fun submit(work: Runnable): FutureTask<Unit> {
        val task = FutureTask(work, Unit)
        if (executor == null) task.run() else executor.execute(task)
        return task
    }

    private fun await(work: List<Future<*>>) {
        try {
            val failures = work.mapNotNull { future ->
                try {
                    future.get()
                    null
                } catch (ex: ExecutionException) {
                    ex.cause ?: ex
                }
            }
            failures.firstOrNull()?.let { first ->
                failures.drop(1).forEach { first.addSuppressed(it) }
                throw first
            }
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
            throw ex
        }
    }

    companion object {

        /**
         * Runs the work one after another on the calling thread.
         */
        fun sequential() = ParallelGroupScheduler(null)

        /**
         * Runs the work on [workers] threads.
         */
        fun fixedPool(workers: Int) = ParallelGroupScheduler(Executors.newFixedThreadPool(workers, WorkerThreadFactory()))
    }

    private class WorkerThreadFactory : ThreadFactory {

        private val counter = AtomicInteger()

        override fun newThread(runnable: Runnable): Thread = Thread(runnable, "skellig-parallel-${counter.incrementAndGet()}")
    }
}
