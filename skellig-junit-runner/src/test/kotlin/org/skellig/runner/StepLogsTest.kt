package org.skellig.runner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.JUnitCore
import org.junit.runner.RunWith
import org.skellig.runner.annotation.SkelligOptions
import org.skellig.runner.steplogs.StepLogRecorderPlugin
import org.skellig.teststep.processing.state.TestScenarioScope

/**
 * Without a parallel run configured, the log records of the step in a report are the same as before scenario scopes
 * had their own log buffers: all the lines logged while the step runs, and the lines of the framework are prefixed
 * with the id of the scope.
 */
class StepLogsTest {

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["step-logs-feature"],
        testSteps = ["step-logs-feature", "org.skellig.runner.steplogs"],
        config = "test.conf"
    )
    @SkelligOptions.Plugin(name = StepLogRecorderPlugin::class, args = ["step-logs"])
    class StepLogsRunner

    @Before
    fun resetRecordedLogs() {
        StepLogRecorderPlugin.stepLogs.clear()
    }

    @Test
    fun testStepLogContainsOnlyLinesLoggedWhileTheStepRuns() {
        val result = JUnitCore().run(StepLogsRunner::class.java)

        assertTrue(result.failures.joinToString { it.message ?: it.toString() }, result.wasSuccessful())
        val firstStepLog = StepLogRecorderPlugin.stepLogs.getValue("Log first message")
        val secondStepLog = StepLogRecorderPlugin.stepLogs.getValue("Log second message")
        assertTrue(firstStepLog.joinToString("\n"), firstStepLog.any { it.endsWith(" - first message") })
        assertFalse(firstStepLog.joinToString("\n"), firstStepLog.any { it.contains("second message") })
        assertTrue(secondStepLog.joinToString("\n"), secondStepLog.any { it.endsWith(" - second message") })
        assertFalse(secondStepLog.joinToString("\n"), secondStepLog.any { it.contains("first message") })
    }

    @Test
    fun testLinesOfTheFrameworkAreMarkedWithTheScopeId() {
        JUnitCore().run(StepLogsRunner::class.java)

        val stepLog = StepLogRecorderPlugin.stepLogs.getValue("Log nothing")
        assertTrue(stepLog.joinToString("\n"), stepLog.any { it.contains(" - [${TestScenarioScope.DEFAULT_ID}]: ") })
        assertFalse(stepLog.joinToString("\n"), stepLog.any { Regex(" - \\[-?\\d+]: ").containsMatchIn(it) })
    }

    @Test
    fun testEveryStepHasItsLog() {
        JUnitCore().run(StepLogsRunner::class.java)

        assertEquals(
            setOf("Log first message", "Log second message", "Log nothing"),
            StepLogRecorderPlugin.stepLogs.keys
        )
    }
}
