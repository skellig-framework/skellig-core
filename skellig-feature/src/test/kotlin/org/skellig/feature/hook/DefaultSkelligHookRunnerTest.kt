package org.skellig.feature.hook

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.skellig.feature.hook.annotation.AfterTestScenario
import org.skellig.feature.hook.annotation.BeforeTestFeature
import org.skellig.feature.hook.annotation.BeforeTestScenario
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.state.TestScenarioScope

class DefaultSkelligHookRunnerTest {

    private val scope = TestScenarioScope("test", DefaultTestScenarioState())

    @Test
    fun testRun() {
        val runner = DefaultSkelligHookRunner(DefaultSkelligTestHooksRegistry(listOf("org.skellig.feature.hook")))

        var fullName = ""
        var error: Throwable? = null
        runner.run(setOf("@T4"), BeforeTestFeature::class.java, scope) { n, e, _ ->
            fullName = n
            error = e
        }

        assertEquals("org.skellig.feature.hook.DefaultSkelligTestHooksRegistryTest\$HookClass.beforeAll", fullName)
        assertNull(error)
    }

    @Test
    fun testRunWithCorrectOrder() {
        val runner = DefaultSkelligHookRunner(DefaultSkelligTestHooksRegistry(listOf("org.skellig.feature.hook")))

        val listOfRunHooks = mutableListOf<String>()
        runner.run(setOf("@T1"), BeforeTestScenario::class.java, scope) { n, _, _ ->
            listOfRunHooks.add(n)
        }

        assertEquals("org.skellig.feature.hook.DefaultSkelligTestHooksRegistryTest\$HookClass.beforeScenario", listOfRunHooks[0])
        assertEquals("org.skellig.feature.hook.DefaultSkelligTestHooksRegistryTest\$HookClass.beforeScenario2", listOfRunHooks[1])
    }

    @Test
    fun testRunWithError() {
        val runner = DefaultSkelligHookRunner(DefaultSkelligTestHooksRegistry(listOf("org.skellig.feature.hook")))

        var fullName = ""
        var error: Throwable? = null
        runner.run(setOf("@T6"), AfterTestScenario::class.java, scope) { n, e, _ ->
            fullName = n
            error = e
        }

        assertEquals("org.skellig.feature.hook.DefaultSkelligTestHooksRegistryTest\$HookClass.afterScenario2", fullName)
        assertEquals("unexpected error", error?.message)
    }

}