package org.skellig.runner.parallelcontext

import org.skellig.teststep.runner.annotation.TestStep
import org.skellig.teststep.runner.context.SkelligTestContext
import org.skellig.teststep.runner.context.SkelligTestContextAware

/**
 * A step definition which still takes the state of the scenario from the context, like it was before parallel runs.
 */
class ContextAwareStepDefs : SkelligTestContextAware {

    private lateinit var context: SkelligTestContext

    @TestStep(name = "Read the state through the context")
    @Suppress("DEPRECATION")
    fun readStateThroughContext() {
        context.getTestScenarioState().set("read", "through the context")
    }

    override fun setSkelligTestContext(context: SkelligTestContext) {
        this.context = context
    }
}
