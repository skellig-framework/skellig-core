package org.skellig.runner.stepdefs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.skellig.feature.hook.annotation.BeforeTestScenario
import org.skellig.teststep.processing.value.function.Function
import org.skellig.teststep.runner.annotation.TestStep
import org.skellig.teststep.runner.context.SkelligTestContext
import org.skellig.teststep.runner.context.SkelligTestContextAware
import org.slf4j.LoggerFactory
import java.util.concurrent.locks.ReentrantLock

class TaskTestStepDefs : SkelligTestContextAware {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(TaskTestStepDefs::class.java)
    }

    private val lock = ReentrantLock()
    private var capacity = 0
    private var insertedCoinsPerClient = mutableMapOf<String, Int>()
    private var context: SkelligTestContext? = null

    @BeforeTestScenario
    fun setUp() {
        capacity = 0
    }

    @TestStep(name = "Insert coin to machine")
    fun insertCoin(parameters: Map<String, Any?>) {
        lock.lock()
        try {
            capacity += parameters["coin"]?.toString()?.toInt() ?: 0
        } finally {
            lock.unlock()
        }
    }

    @TestStep(name = "Client (.+) inserts coin to machine")
    fun clientInsertCoin(client: String, parameters: Map<String, Any?>) {
        lock.lock()
        try {
            insertedCoinsPerClient[client] = insertedCoinsPerClient.getOrDefault(client, 0) + (parameters["coin"]?.toString()?.toInt() ?: 0)
        } finally {
            lock.unlock()
        }
    }

    @TestStep(name = "Verify inserted coins from client (.+) is (\\d+)")
    fun verifyInsertedCoinsFromClients(client: String, total: String) {
        assertEquals(total.toInt(), insertedCoinsPerClient[client])
    }

    @TestStep(name = "Verify machine capacity")
    fun verifyCapacity() {
        assertTrue(capacity < 50, "Capacity is overflow")
    }

    @Function
    fun getMachineCapacity(): Int {
        return capacity
    }

    override fun setSkelligTestContext(context: SkelligTestContext) {
        this.context = context
    }
}