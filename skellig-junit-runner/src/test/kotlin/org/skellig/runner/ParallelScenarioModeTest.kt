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
import org.skellig.runner.parallelrun.ParallelRunRecorder

/**
 * With `skellig.parallel.mode = scenario`, every scenario runs as a parallel group of its own on the worker threads,
 * while the setup and the teardown of its feature run once, around all of its scenarios, in the parallel group of
 * the feature.
 */
class ParallelScenarioModeTest {

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-scenario-feature"],
        testSteps = ["org.skellig.runner.parallelscenario"],
        config = "parallel-scenario-mode.conf"
    )
    class ScenarioModeRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-scenario-feature"],
        testSteps = ["org.skellig.runner.parallelscenario"],
        config = "parallel-scenario-mode-one-thread.conf"
    )
    class OneThreadRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-barrier-feature"],
        testSteps = ["parallel-barrier-feature", "org.skellig.runner.parallelrun"],
        config = "parallel-scenario-mode.conf"
    )
    class TwoFeaturesRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-state-feature"],
        testSteps = ["parallel-state-feature", "org.skellig.runner.parallelrun"],
        config = "parallel-scenario-mode.conf"
    )
    class StateRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-setup-state-feature"],
        testSteps = ["parallel-setup-state-feature", "org.skellig.runner.parallelhooks"],
        config = "parallel-scenario-mode.conf"
    )
    class SetupStateRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-scenario-failing-hook-feature"],
        testSteps = ["org.skellig.runner.parallelscenario"],
        config = "parallel-scenario-mode.conf"
    )
    class FailingHookRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-scenario-failing-setup-feature"],
        testSteps = ["org.skellig.runner.parallelscenario"],
        config = "parallel-scenario-mode.conf"
    )
    class FailingSetupStepRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-scenario-failing-scenario-feature"],
        testSteps = ["org.skellig.runner.parallelscenario"],
        config = "parallel-scenario-mode.conf"
    )
    class FailingScenarioRunner

    private val workerThread = Regex("skellig-parallel-\\d+")

    @Before
    fun resetRecordedRun() {
        ParallelRunRecorder.reset()
    }

    @Test
    fun testScenariosOfAFeatureRunAtTheSameTimeOnTheirOwnWorkerThreadsAndInstances() {
        val result = JUnitCore().run(ScenarioModeRunner::class.java)

        // each scenario waits for the other one at the barrier, so it only passes if they run at the same time
        assertTrue(result.failureReport(), result.wasSuccessful())
        val scenarios = recordedRunsOf("scenario")
        assertEquals(setOf("S1", "S2"), scenarios.map { it.what }.toSet())
        assertEquals(2, scenarios.map { it.thread }.toSet().size)
        assertTrue(scenarios.map { it.thread }.toString(), scenarios.all { workerThread.matches(it.thread) })
        assertNotSame(scenarios[0].instance, scenarios[1].instance)
    }

    @Test
    fun testScenariosOfDifferentFeaturesRunAtTheSameTime() {
        val result = JUnitCore().run(TwoFeaturesRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
        val threads = ParallelRunRecorder.runs.map { it.thread }
        assertEquals(threads.toString(), 2, threads.toSet().size)
        assertTrue(threads.toString(), threads.all { workerThread.matches(it) })
    }

    @Test
    fun testOneWorkerThreadRunsOneScenarioAfterAnother() {
        // there is nobody to meet at the barrier when scenarios do not overlap
        ParallelRunRecorder.reset(parties = 1)

        val result = JUnitCore().run(OneThreadRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
        // the feature takes the only worker thread, so it runs its scenarios itself
        assertEquals(setOf("skellig-parallel-1"), ParallelRunRecorder.runs.map { it.thread }.toSet())
    }

    @Test
    fun testFeatureSetupRunsOnceBeforeItsScenariosAndTeardownOnceAfterTheLastOne() {
        val result = JUnitCore().run(ScenarioModeRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
        // nothing of a scenario, not even its first step at the barrier, runs before the setup has finished
        assertEquals(listOf("before hook", "setup step"), ParallelRunRecorder.runs.take(2).map { it.what })
        val runs = ParallelRunRecorder.runs.filter { it.owner != "barrier" }.map { it.what }
        assertEquals(setOf("S1", "S2"), runs.subList(2, 4).toSet())
        assertEquals(listOf("teardown step", "after hook sees: set in setup"), runs.drop(4))
    }

    @Test
    fun testFeatureSetupAndTeardownRunOnTheInstanceOfTheFeatureWhichNoScenarioUses() {
        val result = JUnitCore().run(ScenarioModeRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
        val ofFeature = recordedRunsOf("feature")
        assertEquals(4, ofFeature.size)
        ofFeature.forEach { assertSame(ofFeature[0].instance, it.instance) }
        recordedRunsOf("scenario").forEach { assertNotSame(ofFeature[0].instance, it.instance) }
        // on the worker thread which runs the feature
        assertEquals(ofFeature.map { it.thread }.toString(), 1, ofFeature.map { it.thread }.toSet().size)
        assertTrue(ofFeature[0].thread, workerThread.matches(ofFeature[0].thread))
    }

    @Test
    fun testJUnitEventsOfAFeatureBracketTheEventsOfItsScenarios() {
        val listener = RecordingRunListener()

        val result = JUnitCore().apply { addListener(listener) }.run(ScenarioModeRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
        val events = listener.events
        val featureStarted = events.indices.filter { events[it].isOf(Type.SUITE_STARTED, "Scenario mode feature") }
        val featureFinished = events.indices.filter { events[it].isOf(Type.SUITE_FINISHED, "Scenario mode feature") }
        val ofScenarios = events.indices.filter { events[it].description.className in setOf("S1", "S2") }
        assertEquals(1, featureStarted.size)
        assertEquals(1, featureFinished.size)
        assertTrue(featureStarted[0] < ofScenarios.min())
        assertTrue(featureFinished[0] > ofScenarios.max())
    }

    @Test
    fun testConcurrentScenariosSeeOnlyTheirOwnState() {
        val result = JUnitCore().run(StateRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
    }

    @Test
    fun testStepsBeforeFeatureSeeStateOfFeatureHooksButScenariosDoNot() {
        val result = JUnitCore().run(SetupStateRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
    }

    @Test
    fun testFailingFeatureHookSkipsScenariosAndTeardownOfTheFeatureAndItsSuiteIsClosed() {
        val listener = RecordingRunListener()

        val result = JUnitCore().apply { addListener(listener) }.run(FailingHookRunner::class.java)

        assertEquals(result.failureReport(), 1, result.failureCount)
        assertEquals("The feature hook failed", result.failures[0].exception.message)
        // only the other feature has run its scenario
        assertEquals(listOf("P1"), recordedRunsOf("scenario").map { it.what })
        assertTrue(recordedRunsOf("feature").none { it.what.endsWith("step") || it.what.startsWith("after hook") })
        assertEquals(1, listener.events.count { it.isOf(Type.SUITE_FINISHED, "Failing hook feature") })
    }

    @Test
    fun testFailingStepBeforeFeatureDoesNotStopItsScenarios() {
        val result = JUnitCore().run(FailingSetupStepRunner::class.java)

        assertEquals(result.failureReport(), 1, result.failureCount)
        assertEquals("The step failed", result.failures[0].exception.message)
        assertEquals(setOf("S1", "S2"), recordedRunsOf("scenario").map { it.what }.toSet())
        assertEquals(listOf("before hook", "teardown step", "after hook sees: set in setup"), recordedRunsOf("feature").map { it.what })
    }

    @Test
    fun testFeatureTeardownRunsOnceAfterTheLastScenarioWhenAScenarioFails() {
        val result = JUnitCore().run(FailingScenarioRunner::class.java)

        assertEquals(result.failureReport(), 1, result.failureCount)
        assertEquals("The step failed", result.failures[0].exception.message)
        val runs = ParallelRunRecorder.runs.map { it.what }
        assertEquals(listOf("before hook", "S2", "teardown step", "after hook sees: set in setup"), runs)
    }

    private fun RecordingRunListener.Event.isOf(type: Type, displayName: String) =
        this.type == type && description.displayName == displayName

    private fun recordedRunsOf(owner: String) = ParallelRunRecorder.runs.filter { it.owner == owner }

}
