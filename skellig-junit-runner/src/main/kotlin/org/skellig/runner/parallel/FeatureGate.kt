package org.skellig.runner.parallel

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Makes sure that the setup of a feature runs exactly once, before any of its scenarios, and its teardown runs
 * exactly once, after the last of them, when the scenarios of the feature run in different parallel groups.
 *
 * The first parallel group which reaches a scenario of the feature [enters][enter] it runs the setup, and every other
 * parallel group waits until the setup has finished. Every scenario then [leaves][leave] the feature when it is done,
 * and the parallel group which leaves it last runs the teardown.
 *
 * The gate knows nothing about the setup and the teardown themselves, and it never waits for another gate, so
 * a parallel group which waits for a setup only waits for a parallel group which is running it.
 *
 * @param scenarios The number of scenarios of the feature which will enter and leave the gate, that are the scenarios
 * which are not excluded by tags.
 */
internal class FeatureGate(scenarios: Int) {

    private val setupStarted = AtomicBoolean(false)
    private val setupFinished = CompletableFuture<Boolean>()
    private val remaining = AtomicInteger(scenarios)

    /**
     * Whether the setup has succeeded. It is the result which all the parallel groups got from [enter].
     */
    val isSetupSucceeded: Boolean
        get() = setupFinished.getNow(false)

    /**
     * Runs the [setup] on the calling thread if this is the first call, otherwise waits until the setup of the first
     * call has finished. The setup is not run inside any lock, and the parallel groups which wait for it are
     * released also when it throws.
     *
     * @param setup Returns whether the scenarios can run, or false, such as when a hook of the feature failed.
     * If it throws, then the scenarios cannot run, and the exception is thrown to the caller which ran it.
     * @return the result of the setup, the same for all the callers.
     * @throws InterruptedException if the caller is interrupted while it waits for the setup, as its scenario
     * cannot be run or skipped silently.
     */
    fun enter(setup: () -> Boolean): Boolean {
        if (setupStarted.compareAndSet(false, true)) {
            var succeeded = false
            try {
                succeeded = setup()
            } finally {
                setupFinished.complete(succeeded)
            }
            return succeeded
        }
        return setupFinished.get()
    }

    /**
     * Counts a scenario of the feature as done, whether it has run, failed, or has been skipped.
     *
     * @return true for the call that counts the last scenario, which must run the teardown.
     */
    fun leave(): Boolean = remaining.decrementAndGet() == 0
}
