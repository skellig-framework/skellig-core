package org.skellig.runner.steplogs

import org.skellig.teststep.processing.util.ScopeLogContext
import org.skellig.teststep.runner.annotation.TestStep
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList

class StepLogStepDefs {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(StepLogStepDefs::class.java)

        /**
         * The id of the scope in the log context of the thread which ran each step that records it.
         */
        val recordedLogScopeIds = CopyOnWriteArrayList<String?>()
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

    @TestStep(name = "Record the log scope")
    fun recordLogScope() {
        recordedLogScopeIds.add(ScopeLogContext.currentScopeId())
    }

    @TestStep(name = "Record the log scope again")
    fun recordLogScopeAgain() {
        recordedLogScopeIds.add(ScopeLogContext.currentScopeId())
    }
}
