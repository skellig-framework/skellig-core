package org.skellig.runner.parallel

import org.junit.runners.model.RunnerScheduler
import org.skellig.teststep.processing.state.ParallelGroupContext
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * Runs features sequentially or on a fixed pool. In tag mode, feature runners submit work on the calling thread;
 * each tag is assigned a single worker's FIFO queue for the whole run. Different tags may share a worker when
 * there are more tags than workers, but a tag never migrates between threads.
 *
 * [finished] drains all submitted work and completion callbacks before releasing workers and group instances.
 */
class ParallelGroupScheduler private constructor(
    private val executor: ExecutorService?,
    private val tagConfig: ParallelConfig? = null,
    private val tagWorkers: List<ExecutorService> = emptyList(),
    private val createGroup: (String) -> ParallelGroupContext = ::ParallelGroupContext
) : RunnerScheduler {
    private val futures = mutableListOf<Future<*>>()
    private val tagAssignments = linkedMapOf<String, Int>()
    private val tagGroups = linkedMapOf<String, ParallelGroupContext>()

    init {
        tagConfig?.tagGroups?.distinct()?.forEach { tag -> tagAssignments[tag] = tagAssignments.size % tagWorkers.size }
    }

    override fun schedule(childStatement: Runnable) {
        // Feature discovery and allocation stay in declaration order. Only their queued work runs on the workers.
        if (tagConfig != null) childStatement.run() else futures.add(submit(childStatement))
    }

    /** Chooses one queue for a scenario; additional matching tags never duplicate its execution. */
    fun tagFor(tags: Set<String>?): String {
        val config = checkNotNull(tagConfig) { "Tag allocation requires a tagged scheduler" }
        val matching = when {
            config.tagGroups.isNotEmpty() -> config.tagGroups.firstOrNull { it in tags.orEmpty() }
            config.tagPattern != null -> tags?.firstOrNull { config.tagPattern.matches(it) }
            else -> tags?.firstOrNull()
        }
        return matching ?: ParallelGroupContext.DEFAULT_KEY
    }

    /** The instances shared by all scenarios assigned to this tag, including across features. */
    fun groupFor(tag: String): ParallelGroupContext {
        check(tagConfig != null) { "Tag allocation requires a tagged scheduler" }
        return tagGroups.getOrPut(tag) { createGroup(tag) }
    }

    /** Enqueues work on the tag's assigned worker and returns without waiting for execution. */
    fun schedule(tag: String, work: Runnable): CompletableFuture<Unit> {
        check(tagConfig != null) { "Tag allocation requires a tagged scheduler" }
        val worker = tagAssignments.getOrPut(tag) { tagAssignments.size % tagWorkers.size }
        val result = CompletableFuture<Unit>()
        futures.add(result)
        tagWorkers[worker].execute {
            try {
                work.run()
                result.complete(Unit)
            } catch (e: Throwable) {
                result.completeExceptionally(e)
            }
        }
        return result
    }

    /** Completes the enclosing suite after its work, even if a submitted task failed. No worker blocks waiting. */
    fun afterAll(work: List<CompletableFuture<Unit>>, completion: () -> Unit) {
        futures.add(CompletableFuture.allOf(*work.toTypedArray()).handle { _, _ -> completion() })
    }

    /**
     * Runs nested scenario-mode work on the same pool. The caller also runs tasks no worker has started yet,
     * so a pool full of feature runners cannot deadlock waiting for its scenarios.
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
            tagWorkers.forEach { it.shutdownNow() }
            throw ex
        } finally {
            executor?.shutdown()
            tagWorkers.forEach { it.shutdown() }
            tagGroups.values.forEach { it.close() }
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
                failures.drop(1).filter { it !== first }.forEach { first.addSuppressed(it) }
                throw first
            }
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
            throw ex
        }
    }

    companion object {
        fun sequential() = ParallelGroupScheduler(null)

        fun fixedPool(workers: Int) = ParallelGroupScheduler(Executors.newFixedThreadPool(workers, WorkerThreadFactory()))

        /** Uses at most the configured number of workers, with one FIFO executor per worker. */
        fun tagged(config: ParallelConfig, createGroup: (String) -> ParallelGroupContext): ParallelGroupScheduler {
            val threads = WorkerThreadFactory()
            val workers = List(config.workersFor()) { Executors.newSingleThreadExecutor(threads) }
            return ParallelGroupScheduler(null, config, workers, createGroup)
        }
    }

    private class WorkerThreadFactory : ThreadFactory {
        private val counter = AtomicInteger()
        override fun newThread(runnable: Runnable): Thread = Thread(runnable, "skellig-parallel-${counter.incrementAndGet()}")
    }
}
