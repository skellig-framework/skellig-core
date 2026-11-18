package org.skellig.runner

import org.junit.Assert.*
import com.typesafe.config.ConfigFactory
import org.junit.Before
import org.junit.Test
import org.junit.runner.JUnitCore
import org.junit.runner.RunWith
import org.skellig.runner.annotation.SkelligOptions
import org.skellig.runner.exception.ParallelConfigException
import org.skellig.runner.parallelrun.ParallelRunRecorder

class ParallelTagModeTest {
    @RunWith(SkelligRunner::class)
    @SkelligOptions(features = ["parallel-tag/1.sf", "parallel-tag/2.sf"],
        testSteps = ["org.skellig.runner.parallelscenario"], config = "parallel-tag.conf")
    class TagRunner

    @Before
    fun reset() { ParallelRunRecorder.reset() }

    @Test
    fun groupsOverlapAndKeepDeclarationOrderAndInstancesAcrossFeatures() {
        val result = JUnitCore().run(TagRunner::class.java)
        assertTrue(result.failureReport(), result.wasSuccessful())
        val runs = ParallelRunRecorder.runs.filter { it.owner == "scenario" }
        val a = runs.filter { it.what.startsWith("A") }
        val b = runs.filter { it.what.startsWith("B") }
        assertEquals(listOf("A1", "A2"), a.map { it.what })
        assertEquals(listOf("B1", "B2"), b.map { it.what })
        assertSame(a[0].instance, a[1].instance)
        assertSame(b[0].instance, b[1].instance)
        assertNotSame(a[0].instance, b[0].instance)
    }
    @Test
    fun patternGroupsRunInParallel() = withSettings("tag.groups" to "", "tag.pattern" to "@A|@B") {
        groupsOverlapAndKeepDeclarationOrderAndInstancesAcrossFeatures()
    }

    @RunWith(SkelligRunner::class)
    @SkelligOptions(features = ["parallel-tag/merge.sf"],
        testSteps = ["org.skellig.runner.parallelscenario", "org.skellig.runner.paralleltag"], config = "parallel-tag.conf")
    class MergeRunner

    @Test
    fun mergingIsTransitivePreservesOrderAndPreventsOverlap() = withSettings("tag.groups" to "@A,@B,@C") {
        ParallelRunRecorder.reset(barrierTimeoutSeconds = 1)
        val result = JUnitCore().run(MergeRunner::class.java)
        assertTrue(result.failureReport(), result.wasSuccessful())
        val runs = ParallelRunRecorder.runs.filter { it.owner == "scenario" }
        assertEquals(listOf("B", "AB", "A", "C", "BC"), runs.map { it.what })
        runs.forEach { assertSame(runs[0].instance, it.instance) }
    }

    @Test
    fun failRejectsMultipleTagsBeforeAnyHooksOrStepsRun() = withSettings("tag.on-multiple" to "fail") {
        val error = assertThrows(ParallelConfigException::class.java) { SkelligRunner(MergeRunner::class.java) }
        assertTrue(error.message, error.message!!.contains("AB"))
        assertTrue(error.message, error.message!!.contains("@A") && error.message!!.contains("@B"))
        assertTrue(ParallelRunRecorder.runs.isEmpty())
    }

    @Test
    fun untaggedScenariosCanEachRunInTheirOwnGroup() = withSettings("tag.groups" to "", "tag.untagged" to "own-group") {
        val result = JUnitCore().run(TagRunner::class.java)
        assertTrue(result.failureReport(), result.wasSuccessful())
        val runs = ParallelRunRecorder.runs.filter { it.owner == "scenario" }
        assertEquals(setOf("A1", "B1", "A2", "B2"), runs.map { it.what }.toSet())
        assertEquals(4, runs.map { it.instance }.toSet().size)
    }

    @RunWith(SkelligRunner::class)
    @SkelligOptions(features = ["parallel-tag/U.sf", "parallel-tag/V.sf"],
        testSteps = ["org.skellig.runner.parallelscenario"], config = "parallel-tag.conf")
    class UntaggedRunner

    @Test
    fun untaggedScenariosCanRunInParallelFeatureGroups() = withSettings("tag.untagged" to "per-feature") {
        val result = JUnitCore().run(UntaggedRunner::class.java)
        assertTrue(result.failureReport(), result.wasSuccessful())
        val runs = ParallelRunRecorder.runs.filter { it.owner == "scenario" }
        val u = runs.filter { it.what.startsWith("U") }
        val v = runs.filter { it.what.startsWith("V") }
        assertEquals(listOf("U1", "U2"), u.map { it.what })
        assertEquals(listOf("V1", "V2"), v.map { it.what })
        assertSame(u[0].instance, u[1].instance)
        assertSame(v[0].instance, v[1].instance)
        assertNotSame(u[0].instance, v[0].instance)
    }

    @Test
    fun explicitGroupsTakePrecedenceOverPattern() = withSettings("tag.pattern" to "@DoesNotMatch") {
        groupsOverlapAndKeepDeclarationOrderAndInstancesAcrossFeatures()
    }

    @Test
    fun oneWorkerRunsGroupsInConfiguredOrderAcrossFeatures() = withSettings("threads" to "1", "tag.groups" to "@B,@A") {
        ParallelRunRecorder.reset(parties = 1)
        val result = JUnitCore().run(TagRunner::class.java)
        assertTrue(result.failureReport(), result.wasSuccessful())
        assertEquals(listOf("B1", "B2", "A1", "A2"), scenarioRuns().map { it.what })
        assertEquals(1, ParallelRunRecorder.runs.map { it.thread }.toSet().size)
    }

    @Test
    fun untaggedScenariosShareOneSequentialGroupByDefault() {
        ParallelRunRecorder.reset(parties = 1)
        val result = JUnitCore().run(UntaggedRunner::class.java)
        assertTrue(result.failureReport(), result.wasSuccessful())
        val runs = scenarioRuns()
        assertEquals(listOf("U1", "U2", "V1", "V2"), runs.map { it.what })
        runs.forEach { assertSame(runs[0].instance, it.instance) }
    }

    @RunWith(SkelligRunner::class)
    @SkelligOptions(features = ["parallel-tag/selection.sf"],
        testSteps = ["org.skellig.runner.parallelscenario"], config = "parallel-tag.conf")
    class SelectionRunner

    @Test
    fun firstUsesConfiguredGroupOrderRatherThanScenarioTagOrder() = withSettings(
        "threads" to "1", "tag.groups" to "@B,@A,@C", "tag.on-multiple" to "first"
    ) {
        val result = JUnitCore().run(SelectionRunner::class.java)
        assertTrue(result.failureReport(), result.wasSuccessful())
        val runs = scenarioRuns()
        assertEquals(listOf("B", "AB", "BC", "A", "C"), runs.map { it.what })
        assertSame(runs[0].instance, runs[1].instance)
        assertSame(runs[0].instance, runs[2].instance)
        assertEquals(3, runs.map { it.instance }.toSet().size)
    }

    @RunWith(SkelligRunner::class)
    @SkelligOptions(features = ["parallel-tag/examples.sf"],
        testSteps = ["org.skellig.runner.parallelscenario"], config = "parallel-tag.conf")
    class ExamplesRunner

    @Test
    fun exampleTableTagsAccumulateOnLaterRowsIncludingUntaggedTables() = withSettings(
        "threads" to "1", "tag.groups" to "@B,@A", "tag.on-multiple" to "first"
    ) {
        val result = JUnitCore().run(ExamplesRunner::class.java)
        assertTrue(result.failureReport(), result.wasSuccessful())
        val runs = scenarioRuns()
        assertEquals(listOf("row2", "row3", "row1"), runs.map { it.what })
        assertSame(runs[0].instance, runs[1].instance)
        assertNotSame(runs[0].instance, runs[2].instance)
    }

    @Test
    fun accumulatedExampleTagsMergeGroupsByDefault() {
        val result = JUnitCore().run(ExamplesRunner::class.java)
        assertTrue(result.failureReport(), result.wasSuccessful())
        val runs = scenarioRuns()
        assertEquals(listOf("row1", "row2", "row3"), runs.map { it.what })
        runs.forEach { assertSame(runs[0].instance, it.instance) }
    }

    @Test
    fun featureGateBracketsScenariosAndKeepsSetupInstancesForTeardown() {
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
        val featureRuns = ParallelRunRecorder.runs.filter { it.owner == "feature" }.groupBy { it.instance }
        assertEquals(2, featureRuns.size)
        featureRuns.forEach { (instance, runs) ->
            assertEquals(listOf("before hook", "setup step", "teardown step", "after hook sees: set in setup"), runs.map { it.what })
            scenarioRuns().forEach { assertNotSame(instance, it.instance) }
        }
    }

    @Test
    fun failedFeatureHookSkipsAllItsGroupsAndClosesItsSuite() = withSettings("mode" to "tag", "tag.untagged" to "own-group") {
        val listener = RecordingRunListener()
        val result = JUnitCore().apply { addListener(listener) }.run(ParallelScenarioModeTest.FailingHookRunner::class.java)
        assertEquals(result.failureReport(), 1, result.failureCount)
        assertEquals("The feature hook failed", result.failures.single().exception.message)
        assertEquals(listOf("P1"), scenarioRuns().map { it.what })
        assertTrue(ParallelRunRecorder.runs.none { it.owner == "feature" && it.what != "before hook" })
        assertEquals(1, listener.events.count { it.type == RecordingRunListener.Type.SUITE_FINISHED && it.description.displayName == "Failing hook feature" })
    }

    @Test
    fun failedSetupStepStillRunsScenariosAndTeardown() = withSettings("mode" to "tag", "tag.untagged" to "own-group") {
        val result = JUnitCore().run(ParallelScenarioModeTest.FailingSetupStepRunner::class.java)
        assertEquals(result.failureReport(), 1, result.failureCount)
        assertEquals("The step failed", result.failures.single().exception.message)
        assertEquals(setOf("S1", "S2"), scenarioRuns().map { it.what }.toSet())
        assertEquals(listOf("before hook", "teardown step", "after hook sees: set in setup"),
            ParallelRunRecorder.runs.filter { it.owner == "feature" }.map { it.what })
    }

    @Test
    fun failedScenarioStillRunsFeatureTeardown() = withSettings("mode" to "tag", "tag.untagged" to "own-group") {
        val result = JUnitCore().run(ParallelScenarioModeTest.FailingScenarioRunner::class.java)
        assertEquals(result.failureReport(), 1, result.failureCount)
        assertEquals("The step failed", result.failures.single().exception.message)
        assertEquals(listOf("S2"), scenarioRuns().map { it.what })
        assertEquals(listOf("teardown step", "after hook sees: set in setup"), ParallelRunRecorder.runs.takeLast(2).map { it.what })
    }

    @RunWith(SkelligRunner::class)
    @SkelligOptions(features = ["parallel-tag/selection.sf"], excludeTags = ["@A", "@C"],
        testSteps = ["org.skellig.runner.parallelscenario"], config = "parallel-tag.conf")
    class FilteredRunner

    @Test
    fun excludedScenariosDoNotCauseMultipleTagErrors() = withSettings("tag.on-multiple" to "fail") {
        val result = JUnitCore().run(FilteredRunner::class.java)
        assertTrue(result.failureReport(), result.wasSuccessful())
        assertEquals(listOf("B"), scenarioRuns().map { it.what })
    }

    @RunWith(SkelligRunner::class)
    @SkelligOptions(features = ["parallel-tag/empty.sf"], excludeTags = ["@Skip"],
        testSteps = ["org.skellig.runner.parallelscenario"], config = "parallel-tag.conf")
    class EmptyRunner

    @Test
    fun featureWithNoSelectedScenariosStillRunsItsLifecycle() {
        val result = JUnitCore().run(EmptyRunner::class.java)
        assertTrue(result.failureReport(), result.wasSuccessful())
        assertEquals(listOf("before hook", "setup step", "teardown step", "after hook sees: set in setup"),
            ParallelRunRecorder.runs.map { it.what })
    }

    @RunWith(SkelligRunner::class)
    @SkelligOptions(features = ["parallel-tag/repeated.sf"],
        testSteps = ["org.skellig.runner.parallelscenario"], config = "parallel-tag.conf")
    class RepeatedNamesRunner

    @Test
    fun repeatedScenarioNamesRetainTheirDistinctTagGroups() {
        ParallelRunRecorder.reset(barrierTimeoutSeconds = 1)
        val result = JUnitCore().run(RepeatedNamesRunner::class.java)
        assertTrue(result.failureReport(), result.wasSuccessful())
        assertEquals(setOf("A", "B"), scenarioRuns().map { it.what }.toSet())
        assertEquals(2, scenarioRuns().map { it.instance }.toSet().size)
    }

    @Test
    fun repeatedScenarioNamesRetainTheirOwnUntaggedGroups() = withSettings("tag.groups" to "", "tag.untagged" to "own-group") {
        repeatedScenarioNamesRetainTheirDistinctTagGroups()
    }

    @RunWith(SkelligRunner::class)
    @SkelligOptions(features = ["parallel-tag/repeated.sf"], includeTags = ["@B"],
        testSteps = ["org.skellig.runner.parallelscenario"], config = "parallel-tag.conf")
    class FilteredRepeatedNamesRunner

    @Test
    fun filteringFirstDeclarationDoesNotLoseLaterSameNamedScenario() {
        ParallelRunRecorder.reset(parties = 1)
        val result = JUnitCore().run(FilteredRepeatedNamesRunner::class.java)
        assertTrue(result.failureReport(), result.wasSuccessful())
        assertEquals(listOf("B"), scenarioRuns().map { it.what })
    }

    @RunWith(SkelligRunner::class)
    @SkelligOptions(features = ["parallel-tag/repeated-conflict.sf"],
        testSteps = ["org.skellig.runner.parallelscenario"], config = "parallel-tag.conf")
    class ConflictingRepeatedNamesRunner

    @Test
    fun laterSameNamedScenarioStillGetsMultipleTagValidation() = withSettings("tag.on-multiple" to "fail") {
        val error = assertThrows(ParallelConfigException::class.java) { SkelligRunner(ConflictingRepeatedNamesRunner::class.java) }
        assertTrue(error.message, error.message!!.contains("@A") && error.message!!.contains("@B"))
        assertTrue(ParallelRunRecorder.runs.isEmpty())
    }

    private fun scenarioRuns() = ParallelRunRecorder.runs.filter { it.owner == "scenario" }

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
