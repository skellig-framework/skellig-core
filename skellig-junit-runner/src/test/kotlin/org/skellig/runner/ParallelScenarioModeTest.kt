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
import org.skellig.runner.parallelrun.ParallelRunRecorder

/**
 * With `skellig.parallel.mode = scenario`, every scenario runs on its own, as its own parallel group on a worker
 * thread, while the setup of a feature runs once, before any of its scenarios, and its teardown runs once, after the
 * last of them, whichever parallel group reaches them.
 */
class ParallelScenarioModeTest {

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-gate-feature"],
        testSteps = ["org.skellig.runner.parallelgate"],
        config = "parallel-scenario-mode.conf"
    )
    class GateRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-gate-one-thread-feature"],
        testSteps = ["org.skellig.runner.parallelgate"],
        config = "parallel-scenario-mode-one-thread.conf"
    )
    class OneThreadRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-gate-failing-scenario-feature"],
        testSteps = ["org.skellig.runner.parallelgate"],
        config = "parallel-scenario-mode.conf"
    )
    class FailingScenarioRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-gate-failing-hook-feature"],
        testSteps = ["org.skellig.runner.parallelgate"],
        config = "parallel-scenario-mode.conf"
    )
    class FailingHookRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-gate-failing-after-hook-feature"],
        testSteps = ["org.skellig.runner.parallelgate"],
        config = "parallel-scenario-mode.conf"
    )
    class FailingAfterHookRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-gate-failing-event-feature"],
        testSteps = ["org.skellig.runner.parallelgate"],
        config = "parallel-scenario-mode.conf"
    )
    @SkelligOptions.Plugin(name = FailingFeaturePlugin::class, args = ["Failing event"])
    class FailingEventRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-gate-failing-before-step-feature"],
        testSteps = ["org.skellig.runner.parallelgate"],
        config = "parallel-scenario-mode.conf"
    )
    class FailingBeforeStepRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-gate-excluded-scenario-feature"],
        testSteps = ["org.skellig.runner.parallelgate"],
        excludeTags = ["@Excluded"],
        config = "parallel-scenario-mode.conf"
    )
    class ExcludedScenarioRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-gate-no-scenarios-feature"],
        testSteps = ["org.skellig.runner.parallelgate"],
        excludeTags = ["@Excluded"],
        config = "parallel-scenario-mode.conf"
    )
    class NoScenariosRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-scenario-state-feature"],
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

    private val workerThread = Regex("skellig-parallel-\\d+")

    @Before
    fun resetRecordedRun() {
        // the three scenarios of the gate feature meet at the barrier
        ParallelRunRecorder.reset(parties = 3)
    }

    @Test
    fun testScenariosOfOneFeatureRunAtTheSameTimeOnTheirOwnWorkerThreads() {
        val result = JUnitCore().run(GateRunner::class.java)

        // the scenarios wait for each other at the barrier, so it only passes if they run at the same time
        assertTrue(result.failureReport(), result.wasSuccessful())
        val threads = recorded("scenario", "meet").map { it.thread }
        assertEquals(threads.toString(), 3, threads.toSet().size)
        assertTrue(threads.toString(), threads.all { workerThread.matches(it) })
    }

    @Test
    fun testEachScenarioRunsOnInstancesOfItsOwn() {
        val result = JUnitCore().run(GateRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
        val instances = ParallelRunRecorder.runs.filter { it.owner == "scenario" && it.what.startsWith("start ") }.map { it.instance }
        assertEquals(3, instances.size)
        assertNotSame(instances[0], instances[1])
        assertNotSame(instances[0], instances[2])
        assertNotSame(instances[1], instances[2])
    }

    @Test
    fun testConcurrentScenariosOfOneFeatureSeeOnlyTheirOwnState() {
        ParallelRunRecorder.reset(parties = 2)

        val result = JUnitCore().run(StateRunner::class.java)

        // both scenarios write their value, meet, and read it back: they only get it back if their state is not shared
        assertTrue(result.failureReport(), result.wasSuccessful())
    }

    @Test
    fun testFeatureSetupRunsOnceAndBeforeAnyScenarioStarts() {
        val result = JUnitCore().run(GateRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
        assertEquals(1, recorded("hook", "before feature started").size)
        assertEquals(1, recorded("hook", "before feature finished").size)
        assertEquals(1, recorded("step", "before feature step sees: set by the setup").size)
        val runs = ParallelRunRecorder.runs
        val setupFinished = runs.indexOfFirst { it.owner == "step" && it.what.startsWith("before feature step") }
        val firstScenarioRun = runs.indexOfFirst { it.owner == "scenario" }
        assertTrue("A scenario has started before the setup has finished: ${describe(runs)}", setupFinished in 0 until firstScenarioRun)
        assertTrue(
            "The step before the feature has run before the hook has finished: ${describe(runs)}",
            runs.indexOfFirst { it.what == "before feature finished" } < setupFinished
        )
    }

    @Test
    fun testFeatureTeardownRunsOnceAfterTheLastScenarioHasFinished() {
        val result = JUnitCore().run(GateRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
        // the first scenario ends only after the other ones, so the teardown has to wait for it though it is not the last one declared
        assertEquals("end 1", ParallelRunRecorder.runs.last { it.owner == "scenario" }.what)
        assertTeardownRanOnceAfterAllScenarios()
    }

    @Test
    fun testFeatureSetupAndTeardownRunOnTheInstancesOfTheFeatureWhichNoScenarioUses() {
        val result = JUnitCore().run(GateRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
        val ofFeature = ParallelRunRecorder.runs.filter { it.owner == "hook" || it.owner == "step" }.map { it.instance }
        // the hook before the feature records its start and its end, the hook after it and the 2 steps record once
        assertEquals(5, ofFeature.size)
        ofFeature.forEach { assertSame(ofFeature[0], it) }
        ParallelRunRecorder.runs.filter { it.owner == "scenario" }.forEach { assertNotSame(ofFeature[0], it.instance) }
    }

    @Test
    fun testJUnitEventsOfAFeatureBracketTheEventsOfItsSetupScenariosAndTeardown() {
        val listener = RecordingRunListener()

        val result = JUnitCore().apply { addListener(listener) }.run(GateRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
        val events = listener.events
        val feature = "Gate feature"
        assertEquals(1, events.count { it.type == Type.SUITE_STARTED && it.description.displayName == feature })
        assertEquals(1, events.count { it.type == Type.SUITE_FINISHED && it.description.displayName == feature })
        val featureStarted = events.indexOfFirst { it.type == Type.SUITE_STARTED && it.description.displayName == feature }
        val featureFinished = events.indexOfFirst { it.type == Type.SUITE_FINISHED && it.description.displayName == feature }
        val beforeFeature = events.indices.filter { events[it].description.className == "Before Feature" }
        val afterFeature = events.indices.filter { events[it].description.className == "After Feature" }
        val scenarios = events.indices.filter { events[it].description.className.startsWith("Gate scenario") }
        assertTrue(beforeFeature.isNotEmpty() && afterFeature.isNotEmpty() && scenarios.isNotEmpty())
        assertTrue("The feature has started after its setup", featureStarted < beforeFeature.min())
        assertTrue("A scenario has started before the step before the feature has finished", beforeFeature.max() < scenarios.min())
        assertTrue("The step after the feature has started before a scenario has finished", scenarios.max() < afterFeature.min())
        assertTrue("The feature has finished before its teardown", afterFeature.max() < featureFinished)
    }

    @Test
    fun testOneWorkerThreadRunsScenariosOneAfterAnotherThroughTheSameGate() {
        val result = JUnitCore().run(OneThreadRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
        val runs = ParallelRunRecorder.runs
        assertEquals(setOf("skellig-parallel-1"), runs.map { it.thread }.toSet())
        assertEquals(1, recorded("hook", "before feature finished").size)
        assertEquals(1, recorded("hook", "after feature sees: set by the setup").size)
        assertEquals(
            listOf("start 1", "end 1", "start 2", "end 2", "start 3", "end 3"),
            runs.filter { it.owner == "scenario" }.map { it.what }
        )
        assertTeardownRanOnceAfterAllScenarios()
    }

    @Test
    fun testFeatureTeardownRunsOnceAfterAllScenariosEvenWhenOneOfThemFails() {
        val result = JUnitCore().run(FailingScenarioRunner::class.java)

        assertEquals(result.failureReport(), 1, result.failureCount)
        assertTrue(result.failureReport(), result.failures[0].exception.message!!.contains("The gate scenario failed"))
        // the other scenario has run to the end
        assertEquals(listOf("end 2", "start 1", "start 2"), recorded("scenario").map { it.what }.sorted())
        assertTeardownRanOnceAfterAllScenarios()
    }

    @Test
    fun testFailingFeatureHookSkipsTheScenariosOfTheFeatureInEveryParallelGroup() {
        val listener = RecordingRunListener()

        val result = JUnitCore().apply { addListener(listener) }.run(FailingHookRunner::class.java)

        // the hook fails once for the feature, and nothing else fails
        assertEquals(result.failureReport(), 1, result.failureCount)
        assertEquals("The feature hook failed", result.failures[0].exception.message)
        assertEquals("Failing hook feature", result.failures[0].description.displayName)
        val runs = ParallelRunRecorder.runs
        assertEquals(1, recorded("hook", "failing before feature").size)
        assertTrue("A scenario of the feature has run: ${describe(runs)}", runs.none { it.what.startsWith("start F") })
        assertTrue("Something after the failing hook has run: ${describe(runs)}", runs.none { it.owner == "step" })
        assertTrue(runs.none { it.what == "after feature of failing hook" })
        assertTrue(listener.events.none { it.description.displayName.startsWith("Failing hook scenario") })
        // the suite of the feature is still closed, once
        assertEquals(1, listener.events.count { it.type == Type.SUITE_STARTED && it.description.displayName == "Failing hook feature" })
        assertEquals(1, listener.events.count { it.type == Type.SUITE_FINISHED && it.description.displayName == "Failing hook feature" })
        // the other feature is not affected, and has its own setup and teardown
        assertEquals(listOf("start H1", "end H1", "start H2", "end H2").sorted(), runs.filter { it.owner == "scenario" }.map { it.what }.sorted())
        assertEquals(1, recorded("hook", "before feature finished").size)
        assertEquals(1, recorded("hook", "after feature sees: set by the setup").size)
    }

    @Test
    fun testFailingStepBeforeFeatureDoesNotStopTheScenarios() {
        val result = JUnitCore().run(FailingBeforeStepRunner::class.java)

        assertEquals(result.failureReport(), 1, result.failureCount)
        assertTrue(result.failureReport(), result.failures[0].exception.message!!.contains("The gate step before the feature failed"))
        assertEquals(listOf("end 1", "end 2", "start 1", "start 2"), recorded("scenario").map { it.what }.sorted())
        assertTeardownRanOnceAfterAllScenarios()
    }

    @Test
    fun testFeatureTeardownDoesNotWaitForScenariosExcludedByTags() {
        val result = JUnitCore().run(ExcludedScenarioRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
        assertEquals(listOf("end 1", "end 3", "start 1", "start 3"), recorded("scenario").map { it.what }.sorted())
        assertTeardownRanOnceAfterAllScenarios()
    }

    @Test
    fun testFeatureWithoutScenariosToRunStillRunsItsSetupAndTeardownOnce() {
        val listener = RecordingRunListener()

        val result = JUnitCore().apply { addListener(listener) }.run(NoScenariosRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
        assertEquals(listOf("before feature finished", "before feature started"), recorded("hook").map { it.what }.filter { it.startsWith("before") }.sorted())
        assertEquals(1, recorded("step", "before feature step sees: set by the setup").size)
        assertEquals(1, recorded("step", "after feature step sees: set by the setup").size)
        assertEquals(1, recorded("hook", "after feature sees: set by the setup").size)
        assertTrue(ParallelRunRecorder.runs.none { it.owner == "scenario" })
        assertEquals(1, listener.events.count { it.type == Type.SUITE_STARTED && it.description.displayName == "No scenarios feature" })
        assertEquals(1, listener.events.count { it.type == Type.SUITE_FINISHED && it.description.displayName == "No scenarios feature" })
    }

    @Test
    fun testFailingFeatureHookAfterTheFeatureIsReportedOnceAndAfterAllScenarios() {
        val listener = RecordingRunListener()

        val result = JUnitCore().apply { addListener(listener) }.run(FailingAfterHookRunner::class.java)

        assertEquals(result.failureReport(), 1, result.failureCount)
        assertEquals("The feature hook after the feature failed", result.failures[0].exception.message)
        assertEquals("Failing after hook feature", result.failures[0].description.displayName)
        assertEquals(listOf("end 1", "end 2", "start 1", "start 2"), recorded("scenario").map { it.what }.sorted())
        assertEquals(1, recorded("step", "after feature step sees: not set by the setup").size)
        assertEquals(1, recorded("hook", "failing after feature").size)
        assertEquals(1, listener.events.count { it.type == Type.SUITE_FINISHED && it.description.displayName == "Failing after hook feature" })
        val featureFinished = listener.events.indexOfFirst { it.type == Type.SUITE_FINISHED && it.description.displayName == "Failing after hook feature" }
        val lastScenarioEvent = listener.events.indexOfLast { it.description.className.startsWith("Failing after hook scenario") }
        assertTrue("The feature has finished before its last scenario", lastScenarioEvent < featureFinished)
    }

    @Test(timeout = 60_000)
    fun testFailureOfASetupEventIsReportedOnceAndDoesNotLeaveScenariosWaiting() {
        val listener = RecordingRunListener()

        val result = JUnitCore().apply { addListener(listener) }.run(FailingEventRunner::class.java)

        // the parallel group which runs the setup gets the failure, and the other ones, which wait for it, skip their scenarios
        assertEquals(result.failureReport(), 1, result.failureCount)
        assertTrue(result.failureReport(), result.failures[0].exception.message!!.startsWith("Report failed for Failing event feature"))
        assertTrue(ParallelRunRecorder.runs.isEmpty())
        // the feature has not started, so there is no suite to close
        assertTrue(listener.events.none { it.description.displayName.startsWith("Failing event") })
    }

    @Test
    fun testStepsBeforeFeatureSeeStateOfFeatureHooksButScenariosDoNot() {
        val result = JUnitCore().run(SetupStateRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
    }

    private fun recorded(owner: String) = ParallelRunRecorder.runs.filter { it.owner == owner }

    private fun recorded(owner: String, what: String) = recorded(owner).filter { it.what == what }

    private fun assertTeardownRanOnceAfterAllScenarios() {
        val runs = ParallelRunRecorder.runs
        // the fields which the setup has set are seen by the teardown
        assertEquals(describe(runs), 1, recorded("step", "after feature step sees: set by the setup").size)
        assertEquals(describe(runs), 1, recorded("hook", "after feature sees: set by the setup").size)
        val steps = runs.indexOfFirst { it.what.startsWith("after feature step") }
        val hooks = runs.indexOfFirst { it.what.startsWith("after feature sees") }
        assertTrue(
            "The teardown has started before the last scenario has finished: ${describe(runs)}",
            runs.indexOfLast { it.owner == "scenario" } in 0 until steps
        )
        assertTrue("The steps after the feature have run after its hooks: ${describe(runs)}", steps < hooks)
    }

    private fun describe(runs: List<ParallelRunRecorder.Run>) = runs.joinToString { "${it.owner}:${it.what}@${it.thread}" }

}
