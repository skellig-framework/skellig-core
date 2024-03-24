package org.skellig.runner.stepdefs

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.fail
import org.skellig.feature.hook.annotation.AfterTestFeature
import org.skellig.feature.hook.annotation.AfterTestScenario
import org.skellig.feature.hook.annotation.BeforeTestFeature
import org.skellig.feature.hook.annotation.BeforeTestScenario
import org.skellig.teststep.runner.annotation.TestStep
import org.skellig.teststep.runner.context.SkelligTestContext
import org.skellig.teststep.runner.context.SkelligTestContextAware
import org.slf4j.LoggerFactory

class TestStepDefs : SkelligTestContextAware {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(TestStepDefs::class.java)
    }

    private var context: SkelligTestContext? = null
    private var hookRunRegistry = mutableListOf<String>()

    @BeforeTestFeature(tags = ["@Tag_A"])
    fun beforeFeature() {
        hookRunRegistry.add("beforeFeature")
    }

    @BeforeTestScenario(tags = ["@Tag_B"])
    fun beforeScenario() {
        hookRunRegistry.add("beforeScenario")
    }

    @AfterTestScenario(tags = ["@Tag_B"])
    fun afterScenario() {
        hookRunRegistry.add("afterScenario")
    }

    @AfterTestFeature(tags = ["@Tag_A"])
    fun afterFeature() {
        hookRunRegistry.add("afterFeature")

        assertEquals("beforeFeature", hookRunRegistry[0])
        assertEquals("beforeScenario", hookRunRegistry[1])
        assertEquals("afterScenario", hookRunRegistry[2])
        assertEquals("beforeScenario", hookRunRegistry[3])
        assertEquals("afterScenario", hookRunRegistry[4])
        assertEquals("beforeScenario", hookRunRegistry[5])
        assertEquals("afterScenario", hookRunRegistry[6])
        assertEquals("afterFeature", hookRunRegistry[7])
    }

    @TestStep(name = "Log (.+)", id = "log1")
    fun logResult(value: String?, parameters: Map<String, String?>): String {
        assertNotNull(value)
        assertEquals(1, parameters.size)

        LOGGER.debug("Executed logResult test step with value $value")
        return "Log record: $value"
    }

    @TestStep(name = "Check user exist")
    fun checkUserExist() {
        LOGGER.error("User doesn't exist")
        fail("User doesn't exist")
    }

    // verify result from a class-based test step, retrieved from the state.
    @TestStep(name = "Check log printed record: '(.+)'")
    fun checkResultAfterLog(expectedResult: String?) {
        val testScenarioState = context?.getTestScenarioState()
        val result = testScenarioState?.get("log1_result")

        assertEquals(expectedResult, result.toString())
    }

    @TestStep(name = "Test test step with parameters")
    fun testTestStepWithParameters(parameters: Map<String, String?>) {
        val size = parameters["expectedSize"]
        assertEquals(size!!.toInt(), parameters.size)
    }

    @TestStep("Run (.+) with (.*)\\s*parameters")
    fun testTestStepWithManyParameters(value: String?, size: String?, notSupplied: Any?, parameters: Map<String, String?>) {
        assertNull(notSupplied)
        assertNotNull(value)
        assertEquals(size?.trim()?.toInt() ?: 0, parameters.size)
    }

    @TestStep(name = "Verify scenario run counter")
    fun verifyScenarioRunCounter() {
        assertEquals("beforeFeature", hookRunRegistry[0])
        assertTrue(hookRunRegistry.contains("beforeScenario"))
        assertFalse(hookRunRegistry.contains("afterFeature"))
    }

    override fun setSkelligTestContext(context: SkelligTestContext) {
        this.context = context
    }
}