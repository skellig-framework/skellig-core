package org.skellig.runner.stepdefs

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.fail
import org.skellig.teststep.runner.annotation.TestStep
import org.skellig.teststep.runner.context.SkelligTestContext
import org.skellig.teststep.runner.context.SkelligTestContextAware

class TestStepDefs : SkelligTestContextAware {

    private var context: SkelligTestContext? = null

    @TestStep(name = "Log (.*)", id = "log1")
    fun logResult(value: String?, parameters: Map<String, String?>): String {
        Assertions.assertNotNull(value)
        Assertions.assertEquals(1, parameters.size)

        println(value)
        return "Log record: $value"
    }

    @TestStep(name = "Check user exist")
    fun checkUserExist() {
        fail("User doesn't exist")
    }

    // verify result from a class-based test step, retrieved from the state.
    @TestStep(name = "Check log printed record: '(.+)'")
    fun checkResultAfterLog(expectedResult: String?) {
        val testScenarioState = context?.getTestScenarioState()
        val result = testScenarioState?.get("log1_result")

        Assertions.assertEquals(expectedResult, result.toString())
    }

    @TestStep(name = "Test test step with parameters")
    fun testTestStepWithParameters(parameters: Map<String, String?>) {
        val size = parameters["expectedSize"]
        Assertions.assertEquals(size!!.toInt(), parameters.size)
    }

    @TestStep(name = "Run (.+) with (.*)\\s*parameters")
    fun testTestStepWithManyParameters(value: String?, size: String?, notSupplied: Any?, parameters: Map<String, String?>) {
        Assertions.assertNull(notSupplied)
        Assertions.assertNotNull(value)
        Assertions.assertEquals(size?.trim()?.toInt() ?: 0, parameters.size)
    }

    override fun setSkelligTestContext(context: SkelligTestContext) {
        this.context = context
    }
}