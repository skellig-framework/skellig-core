package org.skellig.runner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.manipulation.Sorter
import org.mockito.kotlin.mock
import org.skellig.runner.annotation.SkelligOptions
import org.skellig.runner.exception.FeatureRunnerException


class SkelligRunnerTest {

    @Test
    fun testDefaultTags() {
        val skelligRunner = SkelligRunner(SkelligRunnerWithTagsTest::class.java)
        val features = mutableListOf<FeatureRunner>()
        val testScenarios = mutableListOf<TestScenarioRunner>()
        val sorter = createTestSorter(features, testScenarios)

        skelligRunner.sort(sorter)
        assertEquals(1, features.size)
        features[0].sort(sorter)
        assertEquals(2, testScenarios.size)
        assertEquals("Test scenario", testScenarios[0].getEntityName())
        assertEquals("Another test scenario", testScenarios[1].getEntityName())
        assertTrue(testScenarios[1].getEntityTags()?.contains("Extra") == false)
    }

    @Test
    fun testOverriddenTags() {
        System.setProperty("skellig.includeTags", "@T3, @T2")
        val skelligRunner = SkelligRunner(SkelligRunnerWithTagsTest::class.java)
        val features = mutableListOf<FeatureRunner>()
        val testScenarios = mutableListOf<TestScenarioRunner>()
        val sorter = createTestSorter(features, testScenarios)
        skelligRunner.sort(sorter)
        System.setProperty("skellig.includeTags", "")

        assertEquals(1, features.size)
        features[0].sort(sorter)
        assertEquals(1, testScenarios.size)
        assertEquals("Another test scenario", testScenarios[0].getEntityName())
        assertTrue(testScenarios[0].getEntityTags()?.contains("Extra") == false)
    }

    @Test
    fun testRunnerWhenConfigFileNameIsParametrised() {
        System.setProperty("config.name", "test")
        val skelligRunner = SkelligRunner(SkelligRunnerWithParametrisedConfigNameTest::class.java)
        val features = mutableListOf<FeatureRunner>()
        val testScenarios = mutableListOf<TestScenarioRunner>()
        val sorter = createTestSorter(features, testScenarios)
        skelligRunner.sort(sorter)

        assertTrue(features.isNotEmpty())
    }

    @Test
    fun testRunnerWhenReadInvalidFeature() {
        val ex = assertThrows<FeatureRunnerException> { SkelligRunner(SkelligRunnerWithInvalidFeatureTest::class.java) }

        assertEquals("Failed to read features from path: invalid-feature", ex.message)
    }

    @Test
    fun testRunnerWhenReadInvalidFeaturePath() {
        val ex = assertThrows<FeatureRunnerException> { SkelligRunner(SkelligRunnerWithNoFeaturePathTest::class.java) }

        assertEquals("Failed to read features from path: no-feature-path", ex.message)
    }

    @Test
    fun testRunnerWhenReadNoFeaturePath() {
        val ex = assertThrows<FeatureRunnerException> { SkelligRunner(SkelligRunnerWithInvalidFeaturePathTest::class.java) }

        assertEquals("Failed to read features from path: invalid-feature-path", ex.message)
    }

    private fun createTestSorter(features: MutableList<FeatureRunner>, testScenarios: MutableList<TestScenarioRunner>):Sorter {
        return object : Sorter(mock()) {
            override fun apply(target: Any?) {
                if(target is FeatureRunner) {
                    features.add(target)
                }else if (target is TestScenarioRunner){
                    testScenarios.add(target)
                }
            }
        }
    }

    @SkelligOptions(features = ["tags-tests", "feature"],
        testSteps = ["feature", "org.skellig.runner.stepdefs"],
        config = "test.conf",
        includeTags = ["@SmokeTestGroup"],
        excludeTags = ["@Extra"]
    )
    class SkelligRunnerWithTagsTest

    @SkelligOptions(features = ["tags-tests", "feature"],
        testSteps = ["feature", "org.skellig.runner.stepdefs"],
        config = "\${config.name}.conf"
    )
    class SkelligRunnerWithParametrisedConfigNameTest

    @SkelligOptions(features = ["invalid-feature"],
        testSteps = ["feature", "org.skellig.runner.stepdefs"],
        config = "test.conf"
    )
    class SkelligRunnerWithInvalidFeatureTest

    @SkelligOptions(features = ["no-feature-path"],
        testSteps = ["feature", "org.skellig.runner.stepdefs"],
        config = "test.conf"
    )
    class SkelligRunnerWithNoFeaturePathTest

    @SkelligOptions(features = ["invalid-feature-path"],
        testSteps = ["feature", "org.skellig.runner.stepdefs"],
        config = "test.conf"
    )
    class SkelligRunnerWithInvalidFeaturePathTest
}