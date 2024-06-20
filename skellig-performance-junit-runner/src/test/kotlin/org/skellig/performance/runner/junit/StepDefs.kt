package org.skellig.performance.runner.junit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.fail
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.runner.annotation.TestStep
import org.skellig.teststep.runner.context.SkelligTestContext
import org.skellig.teststep.runner.context.SkelligTestContextAware
import java.util.concurrent.locks.ReentrantLock

class StepDefs : SkelligTestContextAware {

    private var testScenarioState: TestScenarioState? = null
    private val lock = ReentrantLock()

    @TestStep("run something")
    fun runSomething() {
        lock.lock()
        try {
            val value = (testScenarioState!!.get("key") as? Int) ?: 0
            testScenarioState!!.set("key", value + 1)
        } finally {
            lock.unlock()
        }
    }

    @TestStep("validate data")
    fun runValidateData() {
        val value = testScenarioState!!.get("key") as Int
        // 100 requests per seconds for 5 seconds with lock, should total 500
        assertEquals(500, value)
    }

    @TestStep("validate failed data")
    fun runValidateFailedData() {
        fail("Failed to validate data")
    }

    override fun setSkelligTestContext(context: SkelligTestContext) {
        testScenarioState = context.getTestScenarioState()
    }
}