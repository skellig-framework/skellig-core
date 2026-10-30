package org.skellig.runner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.JUnitCore
import org.junit.runner.RunWith
import org.skellig.runner.annotation.SkelligOptions
import org.skellig.runner.config.SimpleTestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioScope

/**
 * Without a parallel run configured, every step of every scenario, as well as of the feature setup and teardown,
 * runs in one default scope which wraps the only state of the context.
 */
class DefaultScopeTest {

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["scope-feature"],
        testSteps = ["scope-feature"],
        config = "test.conf"
    )
    class ScopeCheckRunner

    @Test
    fun testAllStepsRunInTheDefaultScopeWrappingTheStateOfTheContext() {
        val result = JUnitCore().run(ScopeCheckRunner::class.java)

        assertTrue(result.failures.joinToString { it.message ?: it.toString() }, result.wasSuccessful())
        // before feature + 3 steps of the scenarios + after feature
        assertEquals(5, SimpleTestStepProcessor.processedScopes.get())
    }
}
