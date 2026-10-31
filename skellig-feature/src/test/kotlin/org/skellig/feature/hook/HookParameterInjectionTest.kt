package org.skellig.feature.hook

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.feature.exception.SkelligClassInstanceRegistryException
import org.skellig.feature.hook.annotation.AfterTestFeature
import org.skellig.feature.hook.annotation.BeforeTestFeature
import org.skellig.feature.hook.annotation.BeforeTestScenario
import org.skellig.feature.injection.InjectedHooks
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.state.TestScenarioScope

class HookParameterInjectionTest {

    private val state = DefaultTestScenarioState()
    private val scope = TestScenarioScope("s1", state)
    private val runner = DefaultSkelligHookRunner(DefaultSkelligTestHooksRegistry(listOf("org.skellig.feature.injection")))

    @Test
    fun testHookWithStateParameterReceivesStateOfScope() {
        runner.run(setOf("@state"), BeforeTestScenario::class.java, scope) { _, e, _ -> assertNull(e) }

        assertSame(state, InjectedHooks.calls["withState"])
    }

    @Test
    fun testHookWithScopeParameterReceivesScope() {
        runner.run(setOf("@scope"), BeforeTestScenario::class.java, scope) { _, e, _ -> assertNull(e) }

        assertSame(scope, InjectedHooks.calls["withScope"])
    }

    @Test
    fun testHookWithBothParametersAtAnyPosition() {
        runner.run(setOf("@both"), BeforeTestScenario::class.java, scope) { _, e, _ -> assertNull(e) }

        assertEquals(listOf(scope, state), InjectedHooks.calls["withBoth"])
    }

    @Test
    fun testHookWithoutParametersStillRuns() {
        var completed = false
        runner.run(setOf("@none"), BeforeTestFeature::class.java, scope) { _, e, _ ->
            assertNull(e)
            completed = true
        }

        assertTrue(completed)
        assertEquals(true, InjectedHooks.calls["withoutParameters"])
    }

    @Test
    fun testHookFailureIsReportedWithInjectedState() {
        state.set("k", "v")
        var error: Throwable? = null
        runner.run(setOf("@fails"), AfterTestFeature::class.java, scope) { _, e, _ -> error = e }

        assertEquals("hook failed with v", error?.message)
    }

    @Test
    fun testHookWithUnsupportedParameterFailsAtStartup() {
        val ex = assertThrows<SkelligClassInstanceRegistryException> {
            DefaultSkelligTestHooksRegistry(listOf("org.skellig.feature.injectioninvalid"))
        }

        assertEquals(
            "Hook method 'org.skellig.feature.injectioninvalid.HookWithUnsupportedParameter.withObject' has a parameter" +
                    " of type 'java.lang.Object' which cannot be injected." +
                    " Hooks can only declare parameters of type TestScenarioState or TestScenarioScope", ex.message
        )
    }
}
