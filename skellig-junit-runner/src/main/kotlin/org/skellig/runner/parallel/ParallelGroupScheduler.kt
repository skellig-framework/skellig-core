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
 * threads, named `skellig-parallel-<n>` unless another name is given. A parallel group is one piece of work, so its scenarios run in order on the
 * thread that runs it.
 *
 * Several schedulers can share the worker threads of one pool, see [on]: each of them waits for its own work only, and
 * the one who created the pool shuts it down.
 *
 * [finished] waits for all the work, and then throws the first exception which the work threw, as nothing in the
 * work is expected to throw. The first one is the one of the work that was scheduled first, and the exceptions
 * of the rest of the work are added to it as suppressed.
 */
class ParallelGroupScheduler private constructor(
    private val executor: ExecutorService?,
    private val ownsExecutor: Boolean = true
) : RunnerScheduler {

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
            if (ownsExecutor) executor?.shutdownNow() else futures.forEach { it.cancel(true) }
            Thread.currentThread().interrupt()
            throw ex
        } finally {
            if (ownsExecutor) executor?.shutdown()
        }
    }

    companion object {

        /**
         * Runs the work one after another on the calling thread.
         */
        fun sequential() = ParallelGroupScheduler(null)

        /**
         * Runs the work on [workers] threads of its own, named `<threadName>-<n>`.
         */
        fun fixedPool(workers: Int, threadName: String = WORKER_THREAD_NAME) = ParallelGroupScheduler(workerPool(workers, threadName))

        /**
         * Runs the work on the threads of the [workerPool], which other schedulers may use at the same time,
         * so it is not shut down when the work is finished.
         */
        fun on(workerPool: ExecutorService) = ParallelGroupScheduler(workerPool, ownsExecutor = false)

        /**
         * Creates a pool of [workers] threads, named `<threadName>-<n>`, for schedulers to share, see [on].
         */
        fun workerPool(workers: Int, threadName: String = WORKER_THREAD_NAME): ExecutorService =
            Executors.newFixedThreadPool(workers, WorkerThreadFactory(threadName))

        private const val WORKER_THREAD_NAME = "skellig-parallel"
    }

    private class WorkerThreadFactory(private val threadName: String) : ThreadFactory {

        private val counter = AtomicInteger()

        override fun newThread(runnable: Runnable): Thread = Thread(runnable, "$threadName-${counter.incrementAndGet()}")
    }
}
