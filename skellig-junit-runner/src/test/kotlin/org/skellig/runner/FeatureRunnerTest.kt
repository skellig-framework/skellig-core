package org.skellig.runner

import org.junit.Test
import org.junit.runner.notification.RunNotifier
import org.mockito.Mockito.verify
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.skellig.feature.Feature
import org.skellig.feature.TestScenario
import org.skellig.feature.TestStep
import org.skellig.feature.metadata.TagsFilter
import org.skellig.teststep.processing.state.TestScenarioState

class FeatureRunnerTest {

    @Test
    fun testRun() {
        val testScenarioState = mock<TestScenarioState>()
        val feature = createFeature()
        val featureRunner = FeatureRunner(feature, mock(), testScenarioState, TagsFilter(emptySet(), emptySet()), mock(), mock())

        val notifier = mock<RunNotifier>()
        featureRunner.run(notifier)

        verify(notifier).fireTestStarted(argThat { o -> o.displayName == feature.scenarios!![0].name })
        verify(notifier).fireTestFinished(argThat { o -> o.displayName == feature.scenarios!![0].name })
        verify(testScenarioState).clean()
    }


    private fun createFeature(): Feature {
        return Feature.Builder().withName("f1").withScenarios(
            TestScenario.Builder()
                .withName("s1")
                .withStep(TestStep.Builder().withName("test step 1"))
        ).build()
    }
}