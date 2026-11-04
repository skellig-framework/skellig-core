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
 */
class ParallelGroupScheduler private constructor(private val executor: ExecutorService?) : RunnerScheduler {

    private val futures = mutableListOf<Future<*>>()

    override fun schedule(childStatement: Runnable) {
        if (executor == null) {
            // runs here and now, but a failure is kept to be reported when all the work is finished
            futures.add(FutureTask<Unit>(childStatement, Unit).also { it.run() })
        } else {
            futures.add(executor.submit(childStatement))
        }
    }

    override fun finished() {
        try {
            val failures = futures.mapNotNull { future ->
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
            executor?.shutdownNow()
            Thread.currentThread().interrupt()
            throw ex
        } finally {
            executor?.shutdown()
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
