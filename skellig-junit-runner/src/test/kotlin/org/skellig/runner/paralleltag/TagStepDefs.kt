package org.skellig.runner.paralleltag

import org.skellig.runner.parallelrun.ParallelRunRecorder
import org.skellig.teststep.runner.annotation.TestStep
import java.util.concurrent.BrokenBarrierException
import java.util.concurrent.TimeoutException

class TagStepDefs {
    @TestStep(name = "Reject overlapping groups")
    fun rejectOverlap() {
        try {
            ParallelRunRecorder.meet()
        } catch (_: TimeoutException) {
            return
        } catch (_: BrokenBarrierException) {
            return
        }
        error("Merged groups reached the barrier concurrently")
    }
}
