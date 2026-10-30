package org.skellig.teststep.runner

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.runner.context.SkelligTestContext

/**
 * Runs the same test steps in two scopes and checks that each scope's state receives only its own values.
 */
class ScenarioScopeIntegrationTest {

    private val context = SkelligTestContext()
    private lateinit var runner: TestStepRunner
    private val stateA = DefaultTestScenarioState()
    private val stateB = DefaultTestScenarioState()
    private val scopeA = TestScenarioScope("A", stateA)
    private val scopeB = TestScenarioScope("B", stateB)

    @BeforeEach
    fun setUp() {
        runner = context.initialize(javaClass.classLoader, listOf("scope-steps"))
    }

    @AfterEach
    fun tearDown() {
        context.close()
    }

    @Test
    fun testRunWithScope() = verifyEachScopeReceivesOnlyItsOwnValues("Store the value")

    @Test
    fun testRunTestWithoutParameters() = verifyEachScopeReceivesOnlyItsOwnValues("Store the value through runTest without parameters")

    @Test
    fun testRunTestWithParameters() = verifyEachScopeReceivesOnlyItsOwnValues("Store the value through runTest with parameters")

    @Test
    fun testRunTestWithParametersHoldingAnotherScope() =
        verifyEachScopeReceivesOnlyItsOwnValues("Store the value through runTest with parameters of its own scope")

    private fun verifyEachScopeReceivesOnlyItsOwnValues(testStepName: String) {
        runAndExpectPass(testStepName, "A", scopeA)
        runAndExpectPass(testStepName, "B", scopeB)

        assertAll(
            { assertEquals("A", stateA.get("stored")) },
            { assertEquals("B", stateB.get("stored")) },
            { assertNull(context.getDefaultScope().state.get("stored")) },
        )
    }

    @Test
    fun testStepWithoutScopeFails() {
        val ex = assertThrows<IllegalStateException> {
            runner.run("Store the value through runTest with parameters", mapOf("value" to "default"))
        }

        assertEquals(
            "Test step 'Store the value through runTest with parameters' has no scenario scope. A test step must be run with a TestScenarioScope",
            ex.message
        )
    }

    @Test
    fun testDefaultScopeWrapsTheStateOfTheContext() {
        runAndExpectPass("Store the value", "default", context.getDefaultScope())

        assertAll(
            { assertEquals(TestScenarioScope.DEFAULT_ID, context.getDefaultScope().id) },
            { assertEquals("default", context.getDefaultScope().state.get("stored")) },
            { assertNull(stateA.get("stored")) },
        )
    }

    @Test
    fun testStepResultsAreStoredInTheStateOfTheScope() {
        runAndExpectPass("Store the value", "A", scopeA)
        runAndExpectPass("Store the value", "B", scopeB)

        assertAll(
            { assertEquals(stateA, stateA.get("Store the value_result")) },
            { assertEquals(stateB, stateB.get("Store the value_result")) },
            { assertNull(context.getDefaultScope().state.get("Store the value_result")) },
        )
    }

    private fun runAndExpectPass(testStepName: String, value: String, scope: TestScenarioScope) =
        expectPass(runner.run(testStepName, mapOf("value" to value), scope))

    private fun expectPass(result: TestStepProcessor.TestStepRunResult) {
        var error: RuntimeException? = null
        result.subscribe { _, _, e -> error = e }
        assertNull(error, error?.message)
    }
}
