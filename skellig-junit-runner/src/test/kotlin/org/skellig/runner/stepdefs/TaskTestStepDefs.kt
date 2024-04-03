package org.skellig.runner.stepdefs

import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.kotlin.inOrder
import org.skellig.teststep.runner.annotation.TestStep
import org.skellig.teststep.runner.context.SkelligTestContext
import org.skellig.teststep.runner.context.SkelligTestContextAware
import org.slf4j.LoggerFactory

class TaskTestStepDefs : SkelligTestContextAware {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(TaskTestStepDefs::class.java)
    }

    private var capacity = 0
    private var context: SkelligTestContext? = null

    @TestStep(name = "Insert coin to machine")
    fun insertCoin(parameters: Map<String, Any?>) {
        capacity += parameters["coin"]?.toString()?.toInt() ?: 0
    }

    @TestStep(name = "Verify machine capacity")
    fun verifyCapacity() {
        assertTrue(capacity <= 50, "Capacity is overflow")
    }

    override fun setSkelligTestContext(context: SkelligTestContext) {
        this.context = context
    }
}