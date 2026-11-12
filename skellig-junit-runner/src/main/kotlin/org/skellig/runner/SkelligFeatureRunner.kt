package org.skellig.runner

import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.skellig.runner.parallel.ParallelGroup

/**
 * A runner of one feature, which [SkelligRunner] runs, either all at once with [run], or in the [parallel groups][createParallelGroups]
 * which the feature is split into by the parallel mode.
 */
interface SkelligFeatureRunner {

    /**
     * The description of the feature, with the descriptions of its steps before and after it, and of its scenarios.
     */
    fun getDescription(): Description

    /**
     * Runs the whole feature, one scenario after another, on the calling thread.
     */
    fun run(notifier: RunNotifier)

    /**
     * The sequences of scenarios of the feature which run in order on a worker thread, while the sequences of the same
     * feature can run at the same time. Running all of them, in any order, runs the whole feature.
     */
    fun createParallelGroups(): List<ParallelGroup>
}
