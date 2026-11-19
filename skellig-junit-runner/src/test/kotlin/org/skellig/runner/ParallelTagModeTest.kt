package org.skellig.runner

import com.typesafe.config.ConfigFactory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.JUnitCore
import org.junit.runner.RunWith
import org.skellig.runner.annotation.SkelligOptions
import org.skellig.runner.parallelrun.ParallelRunRecorder

class ParallelTagModeTest {
    @RunWith(SkelligRunner::class)
    @SkelligOptions(features = ["parallel-tag/1.sf", "parallel-tag/2.sf"],
        testSteps = ["org.skellig.runner.parallelscenario"], config = "parallel-tag.conf")
    class TagRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(features = ["parallel-tag/cross-1.sf", "parallel-tag/cross-2.sf"],
        testSteps = ["org.skellig.runner.parallelscenario"], config = "parallel-tag.conf")
    class CrossFeatureRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(features = ["parallel-tag/U.sf", "parallel-tag/V.sf"],
        testSteps = ["org.skellig.runner.parallelscenario"], config = "parallel-tag.conf")
    class UntaggedRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(features = ["parallel-tag/selection.sf"],
        testSteps = ["org.skellig.runner.parallelscenario"], config = "parallel-tag.conf")
    class SelectionRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(features = ["parallel-tag/examples.sf"],
        testSteps = ["org.skellig.runner.parallelscenario"], config = "parallel-tag.conf")
    class ExamplesRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(features = ["parallel-tag/repeated.sf"],
        testSteps = ["org.skellig.runner.parallelscenario"], config = "parallel-tag.conf")
    class RepeatedNamesRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(features = ["parallel-tag/repeated.sf"], includeTags = ["@B"],
        testSteps = ["org.skellig.runner.parallelscenario"], config = "parallel-tag.conf")
    class FilteredRepeatedNamesRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(features = ["parallel-tag/empty.sf"], excludeTags = ["@Skip"],
        testSteps = ["org.skellig.runner.parallelscenario"], config = "parallel-tag.conf")
    class EmptyRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(features = ["parallel-tag/failing-hook.sf"],
        testSteps = ["org.skellig.runner.paralleltag"], config = "parallel-tag.conf")
    class FailingHookRunner

    @Before
    fun reset() = ParallelRunRecorder.reset()

    @Test
    fun tagQueuesOverlapAndKeepOrderInstancesAndWorkersAcrossFeatures() {
        assertTagQueues()
    }

    private fun assertTagQueues() {
        runSuccessfully(TagRunner::class.java)
        val a = scenarioRuns().filter { it.what.startsWith("A") }
        val b = scenarioRuns().filter { it.what.startsWith("B") }
        assertEquals(listOf("A1", "A2"), a.map { it.what })
        assertEquals(listOf("B1", "B2"), b.map { it.what })
        assertSame(a[0].instance, a[1].instance)
        assertSame(b[0].instance, b[1].instance)
        assertNotSame(a[0].instance, b[0].instance)
        assertEquals(1, a.map { it.thread }.toSet().size)
        assertEquals(1, b.map { it.thread }.toSet().size)
        assertNotEquals(a[0].thread, b[0].thread)
        assertTrue(scenarioRuns().all { it.thread.matches(Regex("skellig-parallel-\\d+")) })
    }

    @Test
    fun allocationDoesNotWaitForScenariosOfEarlierFeatures() {
        // Each feature has one party. Blocking allocation on feature one would time out at the barrier.
        runSuccessfully(CrossFeatureRunner::class.java)
        assertEquals(setOf("A", "B"), scenarioRuns().map { it.what }.toSet())
    }

    @Test
    fun patternSelectsTagQueues() = withSettings("tag.groups" to "", "tag.pattern" to "@A|@B") {
        assertTagQueues()
    }

    @Test
    fun explicitTagsTakePrecedenceOverPattern() = withSettings("tag.pattern" to "@DoesNotMatch") {
        assertTagQueues()
    }

    @Test
    fun withoutConfiguredGroupsOrPatternScenarioTagsSelectQueues() = withSettings("tag.groups" to "") {
        assertTagQueues()
    }

    @Test
    fun oneWorkerDrainsSubmittedScenariosInOrderWithoutBlockingAllocation() = withSettings("threads" to "1") {
        ParallelRunRecorder.reset(parties = 1)
        runSuccessfully(TagRunner::class.java)
        assertEquals(listOf("A1", "B1", "A2", "B2"), scenarioRuns().map { it.what })
        assertEquals(1, ParallelRunRecorder.runs.map { it.thread }.toSet().size)
    }

    @Test
    fun unmatchedScenariosUseOneDefaultQueue() {
        ParallelRunRecorder.reset(parties = 1)
        runSuccessfully(UntaggedRunner::class.java)
        val runs = scenarioRuns()
        assertEquals(listOf("U1", "U2", "V1", "V2"), runs.map { it.what })
        runs.forEach { assertSame(runs[0].instance, it.instance) }
        assertEquals(1, runs.map { it.thread }.toSet().size)
    }

    @Test
    fun multipleTagsSubmitScenarioOnceToFirstConfiguredMatch() = withSettings("tag.groups" to "@B,@A,@C") {
        runSuccessfully(SelectionRunner::class.java)
        val runs = scenarioRuns().associateBy { it.what }
        assertEquals(5, scenarioRuns().size)
        assertEquals(setOf("B", "AB", "A", "C", "BC"), runs.keys)
        assertSame(runs.getValue("B").instance, runs.getValue("AB").instance)
        assertSame(runs.getValue("B").instance, runs.getValue("BC").instance)
        assertNotSame(runs.getValue("A").instance, runs.getValue("B").instance)
        assertNotSame(runs.getValue("C").instance, runs.getValue("B").instance)
        assertTrue(runs.values.map { it.thread }.toSet().size <= 2)
    }

    @Test
    fun exampleTableTagsAccumulateOnLaterRowsIncludingUntaggedTables() = withSettings("tag.groups" to "@B,@A") {
        runSuccessfully(ExamplesRunner::class.java)
        val rows = scenarioRuns().associateBy { it.what }
        assertEquals(setOf("row1", "row2", "row3"), rows.keys)
        assertSame(rows.getValue("row2").instance, rows.getValue("row3").instance)
        assertNotSame(rows.getValue("row1").instance, rows.getValue("row2").instance)
    }

    @Test
    fun featureSuitesCloseOnlyAfterQueuedScenariosAndTeardown() {
        val listener = RecordingRunListener()
        val result = JUnitCore().apply { addListener(listener) }.run(TagRunner::class.java)
        assertTrue(result.failureReport(), result.wasSuccessful())
        for (number in 1..2) {
            val events = listener.events
            val starts = events.indices.filter { events[it].type == RecordingRunListener.Type.SUITE_STARTED && events[it].description.displayName == "Tag feature $number" }
            val ends = events.indices.filter { events[it].type == RecordingRunListener.Type.SUITE_FINISHED && events[it].description.displayName == "Tag feature $number" }
            val scenarios = events.indices.filter { events[it].description.className in setOf("A$number", "B$number") }
            assertEquals(1, starts.size)
            assertEquals(1, ends.size)
            assertTrue(starts.single() < scenarios.min())
            assertTrue(ends.single() > scenarios.max())
        }
        // Each feature's two queues have their own before/after lifecycle.
        assertEquals(4, ParallelRunRecorder.runs.count { it.what == "setup step" })
        assertEquals(4, ParallelRunRecorder.runs.count { it.what == "after hook sees: set in setup" })
    }

    @Test
    fun failedFeatureHookSkipsOnlyItsTagQueueAndStillClosesTheFeature() {
        val listener = RecordingRunListener()
        val result = JUnitCore().apply { addListener(listener) }.run(FailingHookRunner::class.java)
        assertEquals(result.failureReport(), 1, result.failureCount)
        assertEquals("Feature setup failed", result.failures.single().exception.message)
        assertTrue(ParallelRunRecorder.runs.none { it.what == "A1" || it.what == "A2" })
        assertTrue(ParallelRunRecorder.runs.any { it.what == "B1" })
        assertTrue(ParallelRunRecorder.runs.none { it.owner == "@A" && it.what in setOf("teardown", "after A", "after common") })
        assertTrue(ParallelRunRecorder.runs.any { it.owner == "@B" && it.what == "teardown" })
        assertEquals(1, listener.events.count { it.type == RecordingRunListener.Type.SUITE_FINISHED && it.description.displayName == "Failing tag hook" })
    }

    @Test
    fun failedSetupStepStillRunsScenariosAndTeardown() = withSettings("mode" to "tag") {
        val result = JUnitCore().run(ParallelScenarioModeTest.FailingSetupStepRunner::class.java)
        assertEquals(result.failureReport(), 1, result.failureCount)
        assertEquals(setOf("S1", "S2"), scenarioRuns().map { it.what }.toSet())
        assertEquals(listOf("teardown step", "after hook sees: set in setup"), ParallelRunRecorder.runs.takeLast(2).map { it.what })
    }

    @Test
    fun failedScenarioStillRunsFeatureTeardown() = withSettings("mode" to "tag") {
        val result = JUnitCore().run(ParallelScenarioModeTest.FailingScenarioRunner::class.java)
        assertEquals(result.failureReport(), 1, result.failureCount)
        assertEquals(listOf("S2"), scenarioRuns().map { it.what })
        assertEquals(listOf("teardown step", "after hook sees: set in setup"), ParallelRunRecorder.runs.takeLast(2).map { it.what })
    }

    @Test
    fun noFeatureWorkIsQueuedWhenAllScenariosAreExcluded() {
        runSuccessfully(EmptyRunner::class.java)
        assertTrue(ParallelRunRecorder.runs.isEmpty())
    }

    @Test
    fun repeatedNamesRetainTheirOwnTagQueues() {
        runSuccessfully(RepeatedNamesRunner::class.java)
        assertEquals(setOf("A", "B"), scenarioRuns().map { it.what }.toSet())
        assertEquals(2, scenarioRuns().map { it.instance }.toSet().size)
    }

    @Test
    fun filteringFirstDeclarationDoesNotLoseLaterSameNamedScenario() {
        ParallelRunRecorder.reset(parties = 1)
        runSuccessfully(FilteredRepeatedNamesRunner::class.java)
        assertEquals(listOf("B"), scenarioRuns().map { it.what })
    }

    @RunWith(SkelligRunner::class)
    @SkelligOptions(features = ["parallel-tag/hooks.sf"],
        testSteps = ["org.skellig.runner.paralleltag"], config = "parallel-tag.conf")
    class HookQueueRunner

    @Test
    fun featureHooksAndBlocksRunOnTheirScenarioTagWorker() {
        val result = JUnitCore().run(HookQueueRunner::class.java)
        assertTrue(result.failureReport(), result.wasSuccessful())
        val a = ParallelRunRecorder.runs.filter { it.owner == "@A" }
        val b = ParallelRunRecorder.runs.filter { it.owner == "@B" }
        assertEquals(1, a.count { it.what == "before A" })
        assertEquals(1, a.count { it.what == "after A" })
        assertEquals(0, b.count { it.what == "before A" || it.what == "after A" })
        for (runs in listOf(a, b)) {
            assertEquals(1, runs.count { it.what == "before common" })
            assertEquals(1, runs.count { it.what == "after common" })
            assertEquals(1, runs.count { it.what == "setup" })
            assertEquals(1, runs.count { it.what == "teardown" })
            assertEquals(1, runs.map { it.thread }.toSet().size)
            assertEquals(1, runs.map { it.instance }.toSet().size)
            assertTrue(runs.indexOfFirst { it.what == "setup" } < runs.indexOfFirst { it.what == "before scenario" })
            assertTrue(runs.indexOfLast { it.what == "after scenario" } < runs.indexOfFirst { it.what == "teardown" })
        }
        assertEquals(2, a.count { it.what == "before scenario" })
        assertEquals(1, b.count { it.what == "before scenario" })
    }

    private fun scenarioRuns() = ParallelRunRecorder.runs.filter { it.owner == "scenario" }

    private fun runSuccessfully(runner: Class<*>) {
        val result = JUnitCore().run(runner)
        assertTrue(result.failureReport(), result.wasSuccessful())
    }

    private fun withSettings(vararg settings: Pair<String, String>, block: () -> Unit) {
        val previous = settings.associate { (key, _) -> "skellig.parallel.$key" to System.getProperty("skellig.parallel.$key") }
        try {
            settings.forEach { (key, value) -> System.setProperty("skellig.parallel.$key", value) }
            ConfigFactory.invalidateCaches()
            block()
        } finally {
            previous.forEach { (key, value) -> if (value == null) System.clearProperty(key) else System.setProperty(key, value) }
            ConfigFactory.invalidateCaches()
        }
    }
}
