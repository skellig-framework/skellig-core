package org.skellig.feature.hook

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.skellig.feature.hook.annotation.AfterTestScenario
import org.skellig.feature.hook.annotation.BeforeTestScenario
import org.skellig.feature.instances.InstanceHooks
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.state.ParallelGroupContext
import org.skellig.teststep.processing.state.TestScenarioScope

class HookInstancesTest {

    private val runner = DefaultSkelligHookRunner(DefaultSkelligTestHooksRegistry(listOf("org.skellig.feature.instances")))

    @BeforeEach
    fun setUp() {
        InstanceHooks.seen.clear()
    }

    @Test
    fun testHooksOfOneParallelGroupRunOnTheSameInstanceAndKeepItsFields() {
        val group = ParallelGroupContext("g1")
        val scope1 = TestScenarioScope("s1", DefaultTestScenarioState(), group)
        val scope2 = TestScenarioScope("s2", DefaultTestScenarioState(), group)

        run(BeforeTestScenario::class.java, scope1)
        run(AfterTestScenario::class.java, scope1)
        run(BeforeTestScenario::class.java, scope2)

        assertAll(
            { assertEquals(listOf(1, 2, 3), InstanceHooks.seen.map { it.second }) },
            { assertEquals(1, InstanceHooks.seen.map { System.identityHashCode(it.first) }.toSet().size) },
        )
    }

    @Test
    fun testParallelGroupsGetDifferentInstancesOfTheHookClass() {
        val scope1 = TestScenarioScope("s1", DefaultTestScenarioState(), ParallelGroupContext("g1"))
        val scope2 = TestScenarioScope("s2", DefaultTestScenarioState(), ParallelGroupContext("g2"))

        run(BeforeTestScenario::class.java, scope1)
        run(BeforeTestScenario::class.java, scope2)

        val (first, second) = InstanceHooks.seen
        assertAll(
            { assertNotSame(first.first, second.first) },
            { assertEquals(1, first.second) },
            { assertEquals(1, second.second) },
        )
    }

    @Test
    fun testHookInstanceIsTheOneOfTheParallelGroupOfTheScope() {
        val group = ParallelGroupContext("g1")

        run(BeforeTestScenario::class.java, TestScenarioScope("s1", DefaultTestScenarioState(), group))

        assertSame(group.instanceOf(InstanceHooks::class.java), InstanceHooks.seen.single().first)
    }

    @Test
    fun testClosingTheParallelGroupDropsTheHookInstance() {
        val group = ParallelGroupContext("g1")
        val scope = TestScenarioScope("s1", DefaultTestScenarioState(), group)
        run(BeforeTestScenario::class.java, scope)

        group.close()
        run(BeforeTestScenario::class.java, scope)

        val (first, second) = InstanceHooks.seen
        assertAll(
            { assertNotSame(first.first, second.first) },
            { assertEquals(1, second.second) },
        )
    }

    private fun run(hookType: Class<out Annotation>, scope: TestScenarioScope) {
        runner.run(null, hookType, scope) { _, e, _ -> assertNull(e) }
    }
}
