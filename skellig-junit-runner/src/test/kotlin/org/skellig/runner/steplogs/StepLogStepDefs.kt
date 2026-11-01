package org.skellig.runner.steplogs

import org.skellig.teststep.runner.annotation.TestStep
import org.slf4j.LoggerFactory

class StepLogStepDefs {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(StepLogStepDefs::class.java)
    }

    @TestStep(name = "Log first message")
    fun logFirstMessage() {
        // logged by the user code with no knowledge of the scope
        LOGGER.info("first message")
    }

    @TestStep(name = "Log second message")
    fun logSecondMessage() {
        LOGGER.info("second message")
    }

    @TestStep(name = "Log nothing")
    fun logNothing() {
    }
}
