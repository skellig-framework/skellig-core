package org.skellig.teststep.runner.fixture.instances

import org.skellig.teststep.processing.value.function.Function
import org.skellig.teststep.runner.annotation.TestStep
import org.skellig.teststep.runner.context.SkelligTestContext
import org.skellig.teststep.runner.context.SkelligTestContextAware

/**
 * A step definition class with a step method and a function which count the calls in a field,
 * so a test can tell which instance served them.
 */
class GroupSteps : SkelligTestContextAware {

    var context: SkelligTestContext? = null
    var calls = 0

    override fun setSkelligTestContext(context: SkelligTestContext) {
        this.context = context
    }

    @TestStep(name = "count the call")
    fun countTheCall(): Int = ++calls

    @TestStep(name = "has the context")
    fun hasTheContext(): Boolean = context != null

    @Function
    fun callsSoFar(): Int = calls
}
