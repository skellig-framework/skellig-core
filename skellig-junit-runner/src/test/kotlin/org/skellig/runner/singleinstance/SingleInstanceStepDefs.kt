package org.skellig.runner.singleinstance

import org.skellig.feature.hook.annotation.BeforeTestScenario
import org.skellig.teststep.processing.value.function.Function
import org.skellig.teststep.runner.annotation.TestStep
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Records the instance every hook, step and function of this class runs on.
 */
class SingleInstanceStepDefs {

    @BeforeTestScenario
    fun beforeScenario() {
        stepDefExecutionRecorder.add(this)
    }

    @TestStep(name = "Record the step definition instance")
    fun recordStepDefinitionInstance() {
        stepDefExecutionRecorder.add(this)
    }

    @Function
    fun recordFunctionInstance(): Boolean = stepDefExecutionRecorder.add(this)

    companion object {
        val stepDefExecutionRecorder = CopyOnWriteArrayList<Any>()
    }
}
