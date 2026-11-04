package org.skellig.runner.parallel

import org.junit.runner.notification.RunNotifier

/**
 * A sequence of work which runs in order on one worker thread. A parallel group is a feature in the modes `none` and
 * `feature`, where its only entry runs the whole feature, and a single scenario in the mode `scenario`, where its only
 * entry runs the scenario through the gate of its feature.
 *
 * The scenarios are given as the way to run them, so the group knows nothing about features and their setup.
 *
 * @property key Identifies the parallel group in logs.
 */
class ParallelGroup(val key: String, private val scenarios: List<(RunNotifier) -> Unit>) {

    /**
     * Runs the scenarios in order.
     */
    fun run(notifier: RunNotifier) {
        scenarios.forEach { it(notifier) }
    }

    override fun toString(): String = "parallelGroup($key)"
}
