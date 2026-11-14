package org.skellig.runner.parallelscenario

import org.skellig.feature.hook.annotation.AfterTestFeature
import org.skellig.feature.hook.annotation.BeforeTestFeature
import org.skellig.runner.parallelrun.ParallelRunRecorder
import org.skellig.teststep.runner.annotation.TestStep

/**
 * Records where the setup and the teardown of a feature run, and on which instance, in comparison with its scenarios.
 */
class ScenarioModeStepDefs {

    private var setupValue: String? = null

    @BeforeTestFeature(tags = ["@ScenarioMode"])
    fun beforeFeature() {
        setupValue = "set in setup"
        ParallelRunRecorder.record("feature", "before hook", this)
    }

    @AfterTestFeature(tags = ["@ScenarioMode"])
    fun afterFeature() {
        ParallelRunRecorder.record("feature", "after hook sees: $setupValue", this)
    }

    @BeforeTestFeature(tags = ["@FailingHook"])
    fun failingHook() {
        throw IllegalStateException("The feature hook failed")
    }

    @TestStep(name = "Record the (setup|teardown) step")
    fun recordFeatureStep(which: String) {
        ParallelRunRecorder.record("feature", "$which step", this)
    }

    @TestStep(name = "Record the scenario step (\\w+)")
    fun recordScenarioStep(scenario: String) {
        ParallelRunRecorder.record("scenario", scenario, this)
    }

    @TestStep(name = "Meet the other scenario")
    fun meetTheOtherScenario() {
        ParallelRunRecorder.meet()
    }

    @TestStep(name = "Fail the step")
    fun fail() {
        throw IllegalStateException("The step failed")
    }
}
