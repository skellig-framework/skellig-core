package org.skellig.runner.parallelrun

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Lets step definitions of the parallel runs meet at a barrier, which can only be passed if the parties really run at
 * the same time, and keeps what they record, so tests assert on outcomes, not on timing.
 */
object ParallelRunRecorder {

    private const val BARRIER_TIMEOUT_SECONDS = 10L

    /**
     * What one step, or hook, recorded about its run.
     *
     * @property owner Whose run it is, such as the name of a feature.
     * @property what What ran, such as the name of a scenario.
     * @property thread The name of the thread it ran on.
     * @property instance The step definition instance it ran on.
     */
    class Run(val owner: String, val what: String, val thread: String, val instance: Any)

    private val barrier = AtomicReference(CyclicBarrier(2))

    @Volatile
    private var barrierTimeoutSeconds = BARRIER_TIMEOUT_SECONDS

    /**
     * Everything recorded since the last [reset], in the order it was recorded.
     */
    val runs = CopyOnWriteArrayList<Run>()

    /**
     * Forgets everything recorded by the previous test. The barrier has [parties] parties.
     */
    fun reset(parties: Int = 2, barrierTimeoutSeconds: Long = BARRIER_TIMEOUT_SECONDS) {
        runs.clear()
        barrier.set(CyclicBarrier(parties))
        this.barrierTimeoutSeconds = barrierTimeoutSeconds
    }

    /**
     * Blocks until all the parties of the barrier have arrived. It fails if they do not arrive in time,
     * which happens if they never run at the same time.
     */
    fun meet(owner: String = "barrier") {
        record(owner, "meet", this)
        barrier.get().await(barrierTimeoutSeconds, TimeUnit.SECONDS)
    }

    /**
     * Records that [what] of [owner] runs on the current thread, on the [instance].
     */
    fun record(owner: String, what: String, instance: Any) {
        runs.add(Run(owner, what, Thread.currentThread().name, instance))
    }
}
