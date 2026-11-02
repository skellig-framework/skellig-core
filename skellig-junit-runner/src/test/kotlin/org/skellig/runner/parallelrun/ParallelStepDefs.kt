package org.skellig.runner.parallelrun

import org.skellig.teststep.runner.annotation.TestStep

/**
 * Makes steps of different features meet, which only works if the features run at the same time.
 */
class ParallelStepDefs {

    @TestStep(name = "Meet the other feature")
    fun meetTheOtherFeature() {
        ParallelRunRecorder.meet()
    }
}
