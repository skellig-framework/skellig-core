package org.skellig.runner.parallelgate

import org.skellig.feature.hook.annotation.AfterTestFeature
import org.skellig.feature.hook.annotation.BeforeTestFeature
import org.skellig.runner.parallelrun.ParallelRunRecorder
import org.skellig.teststep.runner.annotation.TestStep
import java.util.concurrent.TimeUnit

/**
 * Records where and in which order the setup, the scenarios and the teardown of a feature run, and on which instance of
 * the step definition class. The feature hook takes a while, so a scenario which starts before it has finished
 * shows up in the order of what is recorded.
 */
class GateStepDefs {

    companion object {
        private const val SETUP_MILLIS = 300L
        private const val WAIT_TIMEOUT_SECONDS = 10L
    }

    private var setByTheSetup: String = "not set by the setup"

    @BeforeTestFeature(tags = ["@GateHooks"])
    fun beforeFeature() {
        ParallelRunRecorder.record("hook", "before feature started", this)
        Thread.sleep(SETUP_MILLIS)
        setByTheSetup = "set by the setup"
        ParallelRunRecorder.record("hook", "before feature finished", this)
    }

    @AfterTestFeature(tags = ["@GateHooks"])
    fun afterFeature() {
        ParallelRunRecorder.record("hook", "after feature sees: $setByTheSetup", this)
    }

    @BeforeTestFeature(tags = ["@FailingGateHook"])
    fun failingBeforeFeature() {
        ParallelRunRecorder.record("hook", "failing before feature", this)
        throw IllegalStateException("The feature hook failed")
    }

    @AfterTestFeature(tags = ["@FailingGateHook"])
    fun afterFeatureOfFailingHook() {
        ParallelRunRecorder.record("hook", "after feature of failing hook", this)
    }

    @AfterTestFeature(tags = ["@FailingAfterGateHook"])
    fun failingAfterFeature() {
        ParallelRunRecorder.record("hook", "failing after feature", this)
        throw IllegalStateException("The feature hook after the feature failed")
    }

    @TestStep(name = "Record the gate step before the feature")
    fun recordStepBeforeFeature() {
        ParallelRunRecorder.record("step", "before feature step sees: $setByTheSetup", this)
    }

    @TestStep(name = "Record the gate step after the feature")
    fun recordStepAfterFeature() {
        ParallelRunRecorder.record("step", "after feature step sees: $setByTheSetup", this)
    }

    @TestStep(name = "Meet the other scenarios of the feature")
    fun meetTheOtherScenarios() {
        ParallelRunRecorder.meet("scenario")
    }

    @TestStep(name = "Record the start of gate scenario (\\w+)")
    fun recordStart(scenario: String) {
        ParallelRunRecorder.record("scenario", "start $scenario", this)
    }

    @TestStep(name = "Record the end of gate scenario (\\w+)")
    fun recordEnd(scenario: String) {
        ParallelRunRecorder.record("scenario", "end $scenario", this)
    }

    /**
     * Ends the scenario only after the other ones have ended, so it is the last scenario of the feature to finish,
     * though it may be the first one to start.
     */
    @TestStep(name = "Record the end of gate scenario (\\w+) once gate scenarios (\\w+) and (\\w+) have ended")
    fun recordEndAfterTheOthers(scenario: String, first: String, second: String) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(WAIT_TIMEOUT_SECONDS)
        while (listOf(first, second).any { other -> ParallelRunRecorder.runs.none { it.owner == "scenario" && it.what == "end $other" } }) {
            check(System.nanoTime() < deadline) { "Gate scenarios $first and $second have not ended" }
            Thread.sleep(10)
        }
        recordEnd(scenario)
    }

    @TestStep(name = "Fail the gate scenario")
    fun failScenario() {
        throw IllegalStateException("The gate scenario failed")
    }

    @TestStep(name = "Fail the gate step before the feature")
    fun failStepBeforeFeature() {
        throw IllegalStateException("The gate step before the feature failed")
    }
}
