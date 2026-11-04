package org.skellig.runner.parallelrun

import org.skellig.teststep.runner.annotation.TestStep

/**
 * Records where and in which order steps run, and on which instance of the step definition class.
 */
class OrderStepDefs {

    @TestStep(name = "Record the run of scenario (\\w+) of feature (\\w+)")
    fun recordRun(scenario: String, feature: String) {
        ParallelRunRecorder.record(feature, scenario, this)
    }
}
