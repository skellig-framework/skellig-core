package org.skellig.runner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runner.manipulation.Sorter
import org.mockito.kotlin.mock
import org.skellig.runner.annotation.SkelligOptions


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
        assertEquals("Test scenario", testScenarios[0].testScenario.name)
        assertEquals("Another test scenario", testScenarios[1].testScenario.name)
        assertTrue(testScenarios[1].testScenario.tags?.contains("Extra") == false)
    }

    @Test
    fun testOverriddenTags() {
        System.setProperty("skellig.includeTags", "T2")
        val skelligRunner = SkelligRunner(SkelligRunnerWithTagsTest::class.java)
        val features = mutableListOf<FeatureRunner>()
        val testScenarios = mutableListOf<TestScenarioRunner>()
        val sorter = createTestSorter(features, testScenarios)
        skelligRunner.sort(sorter)

        assertEquals(1, features.size)
        features[0].sort(sorter)
        assertEquals(1, testScenarios.size)
        assertEquals("Another test scenario", testScenarios[0].testScenario.name)
        assertTrue(testScenarios[0].testScenario.tags?.contains("Extra") == false)
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

    @RunWith(SkelligRunner::class)
    @SkelligOptions(features = ["tags-tests", "feature"],
        testSteps = ["feature", "org.skellig.runner.stepdefs"],
        config = "test.conf",
        includeTags = ["SmokeTestGroup"],
        excludeTags = ["Extra"]
    )
    class SkelligRunnerWithTagsTest

}