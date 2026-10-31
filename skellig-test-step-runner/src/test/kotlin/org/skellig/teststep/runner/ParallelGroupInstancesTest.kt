package org.skellig.teststep.runner

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.state.ParallelGroupContext
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.runner.context.SkelligTestContext
import org.skellig.teststep.runner.fixture.instances.GroupSteps

/**
 * Runs test steps and functions of the same class in scopes of different parallel groups, and checks which
 * instance of the class served them.
 */
class ParallelGroupInstancesTest {

    private val context = SkelligTestContext()
    private lateinit var runner: TestStepRunner

    @BeforeEach
    fun setUp() {
        runner = context.initialize(
            javaClass.classLoader,
            listOf("org.skellig.teststep.runner.fixture.instances", "instance-steps")
        )
    }

    @AfterEach
    fun tearDown() {
        context.close()
    }

    @Test
    fun testOneParallelGroupKeepsTheInstanceBetweenScenarios() {
        val group = context.createParallelGroupContext("g1")

        val calls = (1..3).map { countTheCall(scopeOf(group)) }

        assertEquals(listOf(1, 2, 3), calls)
    }

    @Test
    fun testParallelGroupsGetDifferentInstances() {
        val group1 = context.createParallelGroupContext("g1")
        val group2 = context.createParallelGroupContext("g2")

        val calls = listOf(group1, group2, group1, group2, group2).map { countTheCall(scopeOf(it)) }

        assertAll(
            { assertEquals(listOf(1, 1, 2, 2, 3), calls) },
            { assertNotSame(group1.instanceOf(GroupSteps::class.java), group2.instanceOf(GroupSteps::class.java)) },
        )
    }

    @Test
    fun testStepMethodAndFunctionOfTheSameClassShareTheInstanceWithinAParallelGroup() {
        val group1 = context.createParallelGroupContext("g1")
        val group2 = context.createParallelGroupContext("g2")
        countTheCall(scopeOf(group1))
        countTheCall(scopeOf(group1))
        countTheCall(scopeOf(group2))
        val scope1 = scopeOf(group1)
        val scope2 = scopeOf(group2)

        expectPass(runner.run("Read the calls through the function", emptyMap(), scope1))
        expectPass(runner.run("Read the calls through the function", emptyMap(), scope2))

        assertAll(
            { assertEquals(2, scope1.state.get("callsSeenByFunction")) },
            { assertEquals(1, scope2.state.get("callsSeenByFunction")) },
        )
    }

    @Test
    fun testInstancesOfEveryParallelGroupReceiveTheSkelligContext() {
        val group = context.createParallelGroupContext("g1")

        assertAll(
            { assertSame(context, group.instanceOf(GroupSteps::class.java).context) },
            { assertSame(context, context.createParallelGroupContext("g2").instanceOf(GroupSteps::class.java).context) },
        )
    }

    @Test
    fun testStepRunInTheScopeOfAGroupSeesTheContextInItsInstance() {
        val scope = scopeOf(context.createParallelGroupContext("g1"))

        expectPass(runner.run("has the context", emptyMap(), scope))

        assertEquals(true, scope.state.get("has the context_result"))
    }

    @Test
    fun testWithoutParallelRunAllScenariosShareTheInstancesOfTheDefaultParallelGroup() {
        val defaultScope = context.getDefaultScope()

        val calls = (1..3).map { countTheCall(defaultScope) }

        assertAll(
            { assertEquals(listOf(1, 2, 3), calls) },
            { assertEquals(ParallelGroupContext.DEFAULT_KEY, defaultScope.parallelGroup.key) },
            { assertSame(defaultScope.parallelGroup, context.getDefaultScope().parallelGroup) },
        )
    }

    @Test
    fun testFunctionCalledWithoutScopeUsesTheInstancesOfTheDefaultParallelGroup() {
        val defaultScope = context.getDefaultScope()
        countTheCall(defaultScope)
        countTheCall(defaultScope)

        expectPass(runner.run("Read the calls through the function", emptyMap(), defaultScope))

        assertEquals(2, defaultScope.state.get("callsSeenByFunction"))
    }

    @Test
    fun testClosingTheContextDropsTheInstancesOfTheDefaultParallelGroup() {
        val defaultScope = context.getDefaultScope()
        val before = defaultScope.parallelGroup.instanceOf(GroupSteps::class.java)

        context.close()

        assertNotSame(before, defaultScope.parallelGroup.instanceOf(GroupSteps::class.java))
    }

    @Test
    fun testClosingAParallelGroupDropsOnlyItsInstances() {
        val group1 = context.createParallelGroupContext("g1")
        val group2 = context.createParallelGroupContext("g2")
        countTheCall(scopeOf(group1))
        countTheCall(scopeOf(group2))

        group1.close()

        assertAll(
            { assertEquals(1, countTheCall(scopeOf(group1))) },
            { assertEquals(2, countTheCall(scopeOf(group2))) },
        )
    }

    private fun scopeOf(group: ParallelGroupContext) =
        TestScenarioScope("scenario-${System.nanoTime()}", DefaultTestScenarioState(), group)

    private fun countTheCall(scope: TestScenarioScope): Int {
        expectPass(runner.run("count the call", emptyMap(), scope))
        return scope.state.get("count the call_result") as Int
    }

    private fun expectPass(result: TestStepProcessor.TestStepRunResult) {
        var error: RuntimeException? = null
        result.subscribe { _, _, e -> error = e }
        assertNull(error, error?.message)
    }
}
