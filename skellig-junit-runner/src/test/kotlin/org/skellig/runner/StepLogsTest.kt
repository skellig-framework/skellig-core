package org.skellig.runner

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.log4j.MDC
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.JUnitCore
import org.junit.runner.RunWith
import org.skellig.runner.annotation.SkelligOptions
import org.skellig.runner.steplogs.StepLogRecorderPlugin
import org.skellig.runner.steplogs.StepLogStepDefs
import org.skellig.teststep.processing.util.ScopeLogContext
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
        StepLogStepDefs.recordedLogScopeIds.clear()
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
    fun testEveryStepHasItsLog() {
        JUnitCore().run(StepLogsRunner::class.java)

        assertEquals(
            setOf("Log first message", "Log second message", "Log nothing", "Record the log scope in async tasks"),
            StepLogRecorderPlugin.stepLogs.keys
        )
    }

    @Test
    fun testSubTasksOnSharedWorkersAreGivenTheScopeInLogContext() {
        val result = withDefaultDispatcherWorkersOfScope("scope of another run") { JUnitCore().run(StepLogsRunner::class.java) }

        assertTrue(result.failures.joinToString { it.message ?: it.toString() }, result.wasSuccessful())
        // 8 iterations of asyncForEach and 2 steps of asyncEach, none of them runs on the thread of the scenario
        assertEquals(
            List<String?>(10) { TestScenarioScope.DEFAULT_ID },
            StepLogStepDefs.recordedLogScopeIds.toList()
        )
        assertNull(ScopeLogContext.currentScopeId())
    }

    /**
     * Makes the workers of the shared dispatcher have the scope id in their log context while running the [block], like
     * when a scenario created them or ran on them before, and removes it after.
     */
    private fun <T> withDefaultDispatcherWorkersOfScope(scopeId: String, block: () -> T): T {
        setScopeIdOfDefaultDispatcherWorkers(scopeId)
        try {
            return block()
        } finally {
            setScopeIdOfDefaultDispatcherWorkers(null)
        }
    }

    private fun setScopeIdOfDefaultDispatcherWorkers(scopeId: String?) {
        runBlocking {
            withContext(Dispatchers.Default) {
                coroutineScope {
                    repeat(128) {
                        launch {
                            if (scopeId == null) MDC.remove(ScopeLogContext.KEY) else MDC.put(ScopeLogContext.KEY, scopeId)
                            Thread.sleep(20)
                        }
                    }
                }
            }
        }
    }
}
