package org.skellig.runner

import com.typesafe.config.ConfigFactory
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.JUnitCore
import org.junit.runner.Result
import org.junit.runner.RunWith
import org.skellig.runner.annotation.SkelligOptions
import org.skellig.runner.exception.ParallelConfigException
import org.skellig.runner.parallelrun.ParallelRunRecorder
import java.util.concurrent.TimeoutException

/**
 * The parallel run is set by `skellig.parallel` in the configuration of the project, or with system properties.
 * The defaults of the runner apply when a setting is not set, even if there is no configuration file at all.
 */
class ParallelConfigTest {

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-barrier-feature"],
        testSteps = ["parallel-barrier-feature", "org.skellig.runner.parallelrun"]
    )
    class NoConfigFileRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-barrier-feature"],
        testSteps = ["parallel-barrier-feature", "org.skellig.runner.parallelrun"],
        config = "test.conf"
    )
    class NoParallelConfigRunner

    @RunWith(SkelligRunner::class)
    @SkelligOptions(
        features = ["parallel-barrier-feature"],
        testSteps = ["parallel-barrier-feature", "org.skellig.runner.parallelrun"],
        config = "parallel-feature-mode.conf"
    )
    class FeatureModeRunner

    private val parallelProperties = listOf(
        "skellig.parallel", "skellig.parallel.mode", "skellig.parallel.threads", "skellig.parallel.tag.groups",
        "skellig.parallel.tag.groups.0", "skellig.parallel.exclusive-tags", "skellig.parallel.tag.pattern",
        "skellig.parallel.tag.on-multiple", "skellig.parallel.tag.untagged", "skellig.parallel.exclusive-tags.0"
    )

    @Before
    fun resetRecordedRun() {
        // features which meet at the barrier can only pass it if they run at the same time, so a short wait is enough
        // to tell that they do not
        ParallelRunRecorder.reset(barrierTimeoutSeconds = 1)
        setSystemProperties()
    }

    @After
    fun clearSystemProperties() {
        parallelProperties.forEach { System.clearProperty(it) }
        ConfigFactory.invalidateCaches()
    }

    @Test
    fun testNothingRunsInParallelByDefaultWithoutConfigFile() {
        val result = JUnitCore().run(NoConfigFileRunner::class.java)

        assertFeaturesDidNotMeet(result)
    }

    @Test
    fun testNothingRunsInParallelByDefaultWhenConfigFileHasNoParallelSettings() {
        val result = JUnitCore().run(NoParallelConfigRunner::class.java)

        assertFeaturesDidNotMeet(result)
    }

    @Test
    fun testSystemPropertiesTurnParallelRunOnWithoutConfigFile() {
        setSystemProperties("skellig.parallel.mode" to "feature", "skellig.parallel.threads" to "2")
        // the features have to meet, so give them the time that a cold start of a loaded machine may need
        ParallelRunRecorder.reset()

        val result = JUnitCore().run(NoConfigFileRunner::class.java)

        assertTrue(result.failureReport(), result.wasSuccessful())
    }

    @Test
    fun testSystemPropertiesOverrideConfigFile() {
        setSystemProperties("skellig.parallel.mode" to "none")

        val result = JUnitCore().run(FeatureModeRunner::class.java)

        assertFeaturesDidNotMeet(result)
    }

    @Test
    fun testInvalidModeFailsWhenRunnerIsCreated() {
        setSystemProperties("skellig.parallel.mode" to "parallel")

        val ex = assertThrows<ParallelConfigException> { SkelligRunner(NoConfigFileRunner::class.java) }

        assertEquals("Invalid value 'parallel' of 'skellig.parallel.mode', expected one of: none, feature, scenario, tag", ex.message)
    }

    @Test
    fun testInvalidValueOfAnyKeyFailsWhenRunnerIsCreated() {
        listOf(
            "skellig.parallel.threads" to "-1",
            "skellig.parallel.threads" to "many",
            "skellig.parallel.tag.on-multiple" to "all",
            "skellig.parallel.tag.untagged" to "ignore",
            "skellig.parallel.tag.pattern" to "(",
            "skellig.parallel.tag.groups.0" to "payments",
            "skellig.parallel.exclusive-tags.0" to "performance"
        ).forEach { (key, value) ->
            setSystemProperties(key to value)

            val ex = assertThrows<ParallelConfigException>("$key = $value") { SkelligRunner(NoConfigFileRunner::class.java) }

            val keyOfList = key.removeSuffix(".0")
            assertTrue(ex.message, ex.message!!.startsWith("Invalid value '$value' of '$keyOfList'"))
        }
    }

    @Test
    fun testTagsOfSystemPropertiesCanBeCommaSeparated() {
        setSystemProperties("skellig.parallel.tag.groups" to "@payments, @orders", "skellig.parallel.exclusive-tags" to "@performance,@inc")

        SkelligRunner(NoConfigFileRunner::class.java)
    }

    @Test
    fun testInvalidTagInCommaSeparatedTagsFailsWhenRunnerIsCreated() {
        setSystemProperties("skellig.parallel.exclusive-tags" to "@performance,inc")

        val ex = assertThrows<ParallelConfigException> { SkelligRunner(NoConfigFileRunner::class.java) }

        assertEquals("Invalid value 'inc' of 'skellig.parallel.exclusive-tags', expected tags which start with '@'", ex.message)
    }

    @Test
    fun testModesWhichAreNotSupportedYetFailWhenRunnerIsCreated() {
        listOf("scenario", "tag").forEach { mode ->
            setSystemProperties("skellig.parallel.mode" to mode)

            val ex = assertThrows<ParallelConfigException>(mode) { SkelligRunner(NoConfigFileRunner::class.java) }

            assertEquals("Parallel mode '$mode' is not supported yet, the supported modes are: none, feature", ex.message)
        }
    }

    @Test
    fun testParallelSettingsWhichAreNotAnObjectFailWhenRunnerIsCreated() {
        setSystemProperties("skellig.parallel" to "5")

        val ex = assertThrows<ParallelConfigException> { SkelligRunner(NoConfigFileRunner::class.java) }

        assertTrue(ex.message, ex.message!!.startsWith("Invalid settings in 'skellig.parallel'"))
    }

    @Test
    fun testValidValuesOfAllKeysAreAccepted() {
        setSystemProperties(
            "skellig.parallel.mode" to "none", "skellig.parallel.threads" to "0", "skellig.parallel.tag.groups.0" to "@payments",
            "skellig.parallel.tag.pattern" to "^@group-.+", "skellig.parallel.tag.on-multiple" to "first",
            "skellig.parallel.tag.untagged" to "per-feature", "skellig.parallel.exclusive-tags.0" to "@performance"
        )

        SkelligRunner(NoConfigFileRunner::class.java)
    }

    private fun setSystemProperties(vararg properties: Pair<String, String>) {
        parallelProperties.forEach { System.clearProperty(it) }
        properties.forEach { (key, value) -> System.setProperty(key, value) }
        // system properties are read once, unless the caches are invalidated
        ConfigFactory.invalidateCaches()
    }

    private fun assertFeaturesDidNotMeet(result: Result) {
        val causes = result.failures.flatMap { generateSequence<Throwable>(it.exception) { cause -> cause.cause }.toList() }
        assertTrue("The features waiting for each other at the barrier have passed it", causes.any { it is TimeoutException })
    }

}
