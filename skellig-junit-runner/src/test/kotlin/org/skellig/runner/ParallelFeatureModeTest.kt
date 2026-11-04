package org.skellig.runner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.JUnitCore
import org.junit.runner.RunWith
import org.skellig.runner.RecordingRunListener.Type
import org.skellig.runner.annotation.SkelligOptions
import org.skellig.runner.parallelfailure.FailingFeaturePlugin
import org.skellig.runner.parallelhooks.HookLogRecorderPlugin
import org.skellig.runner.parallelrun.ParallelRunRecorder

/**
 * With `skellig.parallel.mode = feature`, features run in parallel on worker threads while the scenarios
 * of each feature run in order, every scenario with a state of its own, and every feature with its own
 * instances of the step definition classes.
 */
class ParallelFeatureModeTest {

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-barrier-feature"],
        testSteps = ["parallel-barrier-feature", "org.skellig.runner.parallelrun"],
        config = "parallel-feature-mode.conf"
    )
    class BarrierRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-state-feature"],
        testSteps = ["parallel-state-feature", "org.skellig.runner.parallelrun"],
        config = "parallel-feature-mode.conf"
    )
    class StateRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-order-feature"],
        testSteps = ["parallel-order-feature", "org.skellig.runner.parallelrun"],
        config = "parallel-feature-mode.conf"
    )
    class OrderRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-order-feature"],
        testSteps = ["parallel-order-feature", "org.skellig.runner.parallelrun"],
        config = "parallel-feature-mode-one-thread.conf"
    )
    class OneThreadRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-hooks-feature"],
        testSteps = ["parallel-hooks-feature", "org.skellig.runner.parallelrun", "org.skellig.runner.parallelhooks"],
        config = "parallel-feature-mode.conf"
    )
    class HooksRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-failure-feature"],
        testSteps = ["parallel-failure-feature", "org.skellig.runner.parallelrun"],
        config = "parallel-feature-mode.conf"
    )
    @SkelligOptions.Plugin(name = FailingFeaturePlugin::class, args = ["Failing feature"])
    class UnexpectedFailureRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-hooks-feature"],
        testSteps = ["parallel-hooks-feature", "org.skellig.runner.parallelrun", "org.skellig.runner.parallelhooks"],
        config = "parallel-feature-mode-with-logs.conf"
    )
    @SkelligOptions.Plugin(name = HookLogRecorderPlugin::class, args = ["hook-logs"])
    class HookLogsRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-setup-state-feature"],
        testSteps = ["parallel-setup-state-feature", "org.skellig.runner.parallelhooks"],
        config = "parallel-feature-mode.conf"
    )
    class SetupStateRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-failure-feature"],
        testSteps = ["parallel-failure-feature", "org.skellig.runner.parallelrun"],
        config = "test.conf"
    )
    @SkelligOptions.Plugin(name = FailingFeaturePlugin::class, args = ["Failing feature"])
    class UnexpectedFailureWithoutParallelRunner

    private val workerThread = Regex("skellig-parallel-\\d+")

    @Before
    fun resetRecordedRun() {
        ParallelRunRecorder.reset()
        HookLogRecorderPlugin.hookLogs.clear()
    }

    @Test
    fun testTwoFeaturesRunAtTheSameTimeOnTheirOwnWorkerThreads() {
        val result = JUnitCore().run(BarrierRunner::class.java)

        // each feature waits for the other one at the barrier, so it only passes if they run at the same time
        assertTrue(result.failureReport(), result.wasSuccessful())
        val threads = ParallelRunRecorder.runs.map { it.thread }
        assertEquals(threads.toString(), 2, threads.toSet().size)
        assertTrue(threads.toString(), threads.all { workerThread.matches(it) })
    }

    @Test
    fun testConcurrentScenariosSeeOnlyTheirOwnState() {
        val result = JUnitCore().run(StateRunner::class.java)

        // both features write their value, meet, and read it back: they only get it back if their state is not shared
        assertTrue(result.failureReport(), result.wasSuccessful())
    }

    @Test
    fun testScenariosOfAFeatureRunInOrderOnOneThread() {
        val result = JUnitCore().run(OrderRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
        val runsOfA = recordedRunsOf("A")
        val runsOfB = recordedRunsOf("B")
        assertEquals(listOf("A1", "A2", "A3"), runsOfA.map { it.what })
        assertEquals(listOf("B1", "B2", "B3"), runsOfB.map { it.what })
        assertEquals(1, runsOfA.map { it.thread }.toSet().size)
        assertEquals(1, runsOfB.map { it.thread }.toSet().size)
        assertTrue(runsOfA[0].thread != runsOfB[0].thread)
    }

    @Test
    fun testFeaturesHaveInstancesOfTheirOwnWhichTheirScenariosShare() {
        val result = JUnitCore().run(OrderRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
        val instancesOfA = recordedRunsOf("A").map { it.instance }
        val instancesOfB = recordedRunsOf("B").map { it.instance }
        instancesOfA.forEach { assertSame(instancesOfA[0], it) }
        instancesOfB.forEach { assertSame(instancesOfB[0], it) }
        assertNotSame(instancesOfA[0], instancesOfB[0])
    }

    @Test
    fun testOneWorkerThreadRunsOneFeatureAfterAnother() {
        // there is nobody to meet at the barrier when features do not overlap
        ParallelRunRecorder.reset(parties = 1)

        val result = JUnitCore().run(OneThreadRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
        val runs = ParallelRunRecorder.runs
        assertEquals(setOf("skellig-parallel-1"), runs.map { it.thread }.toSet())
        // all of one feature, then all of the other one, whichever is first
        val owners = runs.map { it.owner }.filter { it == "A" || it == "B" }
        assertTrue(owners.toString(), owners == listOf("A", "A", "A", "B", "B", "B") || owners == listOf("B", "B", "B", "A", "A", "A"))
    }

    @Test
    fun testHooksOfAFeatureRunInTheParallelGroupOfTheFeature() {
        val result = JUnitCore().run(HooksRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
        val runsByThread = ParallelRunRecorder.runs.filter { it.owner != "barrier" }.groupBy { it.thread }
        assertEquals(2, runsByThread.size)
        runsByThread.values.forEach { runs ->
            // the hooks surround the steps of the feature, and they all run on the instance of the feature
            assertEquals(listOf("before feature", "step", "step", "after feature"), runs.map { it.what })
            runs.forEach { assertSame(runs[0].instance, it.instance) }
        }
        val instances = runsByThread.values.map { it.first().instance }
        assertNotSame(instances[0], instances[1])
    }

    @Test
    fun testJUnitEventsBracketScenariosOfEachFeatureAndAreFiredByTheWorkerOfTheStep() {
        val listener = RecordingRunListener()

        val result = JUnitCore().apply { addListener(listener) }.run(OrderRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
        mapOf("Order feature A" to setOf("A1", "A2", "A3"), "Order feature B" to setOf("B1", "B2", "B3")).forEach { (feature, scenarios) ->
            val events = listener.events
            val featureStarted = events.indexOfFirst { it.type == Type.SUITE_STARTED && it.description.displayName == feature }
            val featureFinished = events.indexOfFirst { it.type == Type.SUITE_FINISHED && it.description.displayName == feature }
            val ofScenarios = events.indices.filter { events[it].description.className in scenarios }
            assertTrue("$feature has no suite events", featureStarted >= 0 && featureFinished >= 0)
            assertTrue("$feature started after one of its scenarios", featureStarted < ofScenarios.min())
            assertTrue("$feature finished before one of its scenarios", featureFinished > ofScenarios.max())
        }
        val stepEvents = listener.events.filter { it.type == Type.TEST_STARTED || it.type == Type.TEST_FINISHED }
        assertTrue(stepEvents.isNotEmpty())
        assertTrue(stepEvents.map { it.thread }.toString(), stepEvents.all { workerThread.matches(it.thread) })
        // the start and the finish of a step are fired by the same thread
        stepEvents.groupBy { it.description }.values.forEach { events -> assertEquals(1, events.map { it.thread }.toSet().size) }
    }

    @Test
    fun testFirstUnexpectedFailureIsReportedAfterAllFeaturesHaveFinished() {
        val result = JUnitCore().run(UnexpectedFailureRunner::class.java)

        assertEquals(result.failureReport(), 1, result.failureCount)
        assertTrue(result.failureReport(), result.failures[0].exception.message!!.startsWith("Report failed for Failing feature"))
        // the feature which does not fail has run, even if another one failed
        assertEquals(listOf("P1"), recordedRunsOf("P").map { it.what })
    }

    @Test
    fun testLogOfAFeatureHookContainsOnlyLinesOfThatHook() {
        val result = JUnitCore().run(HookLogsRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
        // 2 features, each with a hook before and a hook after
        assertEquals(4, HookLogRecorderPlugin.hookLogs.size)
        HookLogRecorderPlugin.hookLogs.forEach { hookLog ->
            assertEquals(
                hookLog.logRecords.toString(), 1,
                hookLog.logRecords.count { it.endsWith("The hook runs on ${hookLog.thread}") }
            )
            assertEquals(hookLog.logRecords.toString(), 1, hookLog.logRecords.count { it.contains("The hook runs on") })
        }
    }

    @Test
    fun testStepsBeforeFeatureSeeStateOfFeatureHooksButScenariosDoNot() {
        val result = JUnitCore().run(SetupStateRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
    }

    @Test
    fun testFirstUnexpectedFailureIsReportedAfterAllFeaturesHaveFinishedWithoutParallelRun() {
        val result = JUnitCore().run(UnexpectedFailureWithoutParallelRunner::class.java)

        assertEquals(result.failureReport(), 1, result.failureCount)
        assertTrue(result.failureReport(), result.failures[0].exception.message!!.startsWith("Report failed for Failing feature"))
        assertEquals(listOf("P1"), recordedRunsOf("P").map { it.what })
    }

    private fun recordedRunsOf(feature: String) = ParallelRunRecorder.runs.filter { it.owner == feature }

}
