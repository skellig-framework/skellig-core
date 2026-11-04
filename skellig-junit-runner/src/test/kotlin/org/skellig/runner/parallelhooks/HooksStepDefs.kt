package org.skellig.runner.parallelhooks

import org.skellig.feature.hook.annotation.AfterTestFeature
import org.skellig.feature.hook.annotation.BeforeTestFeature
import org.skellig.runner.parallelrun.ParallelRunRecorder
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.runner.annotation.TestStep
import org.slf4j.LoggerFactory

/**
 * Records where the hooks of a feature run, and on which instance, in comparison with its steps.
 */
class HooksStepDefs {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(HooksStepDefs::class.java)
    }

    @BeforeTestFeature(tags = ["@ParallelHooks"])
    fun beforeFeature() {
        LOGGER.info("The hook runs on ${Thread.currentThread().name}")
        ParallelRunRecorder.record("hook", "before feature", this)
    }

    @AfterTestFeature(tags = ["@ParallelHooks"])
    fun afterFeature() {
        LOGGER.info("The hook runs on ${Thread.currentThread().name}")
        ParallelRunRecorder.record("hook", "after feature", this)
    }

    @BeforeTestFeature(tags = ["@SetupState"])
    fun storeInStateOfFeature(state: TestScenarioState) {
        state.set("setupValue", "stored")
    }

    @TestStep(name = "Record the run in the hooks feature (\\w+)")
    fun recordRun(feature: String) {
        ParallelRunRecorder.record(feature, "step", this)
    }
}
