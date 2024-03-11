package org.skellig.runner

import org.junit.Test
import org.junit.runner.notification.RunNotifier
import org.mockito.Mockito
import org.mockito.kotlin.*
import org.skellig.feature.TestScenario
import org.skellig.feature.TestStep
import org.skellig.feature.hook.SkelligHookRunner
import org.skellig.feature.hook.annotation.AfterTestScenario
import org.skellig.feature.hook.annotation.BeforeTestScenario
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.runner.TestStepRunner

class TestScenarioRunnerTest {

    private val hookRunner = mock<SkelligHookRunner>()
    private val testStepRunner = mock<TestStepRunner>()
    private val testScenario = createScenarioWithBeforeAndAfterSteps()
    private val scenarioRunner = TestScenarioRunner.create(testScenario, testStepRunner, hookRunner, mock())

    @Test
    fun testRunAndVerifyFeatureBeforeAndAfterTestStepsRun() {
        val result1 = mock<TestStepProcessor.TestStepRunResult>()
        val result2 = mock<TestStepProcessor.TestStepRunResult>()
        val result3 = mock<TestStepProcessor.TestStepRunResult>()
        val result4 = mock<TestStepProcessor.TestStepRunResult>()
        val result5 = mock<TestStepProcessor.TestStepRunResult>()
        whenever(testStepRunner.run(testScenario.beforeSteps!![0].name, emptyMap())).thenReturn(result1)
        whenever(testStepRunner.run(testScenario.beforeSteps!![1].name, emptyMap())).thenReturn(result2)
        whenever(testStepRunner.run(testScenario.afterSteps!![0].name, emptyMap())).thenReturn(result3)
        whenever(testStepRunner.run(testScenario.afterSteps!![1].name, emptyMap())).thenReturn(result4)
        whenever(testStepRunner.run(testScenario.steps!![0].name, emptyMap())).thenReturn(result5)

        val notifier = mock<RunNotifier>()
        scenarioRunner.run(notifier)

        val orderVerifier = Mockito.inOrder(notifier)
        orderVerifier.verify(notifier, times(1)).fireTestStarted(argThat { o -> o.displayName == "${testScenario.beforeSteps!![0].name}(${testScenario.name})" })
        orderVerifier.verify(notifier, times(1)).fireTestStarted(argThat { o -> o.displayName == "${testScenario.beforeSteps!![1].name}(${testScenario.name})" })
        orderVerifier.verify(notifier, times(1)).fireTestStarted(argThat { o -> o.displayName == "${testScenario.afterSteps!![0].name}(${testScenario.name})" })
        orderVerifier.verify(notifier, times(1)).fireTestStarted(argThat { o -> o.displayName == "${testScenario.afterSteps!![1].name}(${testScenario.name})" })

        org.junit.jupiter.api.assertAll(
            { Mockito.verify(result1).subscribe(argThat { true }) },
            { Mockito.verify(result2).subscribe(argThat { true }) },
            { Mockito.verify(result3).subscribe(argThat { true }) },
            { Mockito.verify(result4).subscribe(argThat { true }) },
            { Mockito.verify(result5).subscribe(argThat { true }) },
            { Mockito.verify(result1).awaitResult() },
            { Mockito.verify(result2).awaitResult() },
            { Mockito.verify(result3).awaitResult() },
            { Mockito.verify(result4).awaitResult() },
            { Mockito.verify(result5).awaitResult() },
        )
    }

    @Test
    fun testRunAndVerifyHookOrderRunIsCorrect() {
        scenarioRunner.run(mock<RunNotifier>())

        val orderVerifier = Mockito.inOrder(hookRunner)
        orderVerifier.verify(hookRunner).run(eq(testScenario.tags), eq(BeforeTestScenario::class.java), argThat { true })
        orderVerifier.verify(hookRunner).run(eq(testScenario.tags), eq(AfterTestScenario::class.java), argThat { true })
    }

    private fun createScenarioWithBeforeAndAfterSteps(): TestScenario {
        val testStepBeforeName1 = "test step before 1"
        val testStepBeforeName2 = "test step before 2"
        val testStepAfterName1 = "test step after 1"
        val testStepAfterName2 = "test step after 2"

        return TestScenario.Builder()
            .withName("f1")
            .withBeforeSteps(
                listOf(
                    TestStep.Builder().withName(testStepBeforeName1).build(),
                    TestStep.Builder().withName(testStepBeforeName2).build()
                )
            )
            .withAfterSteps(
                listOf(
                    TestStep.Builder().withName(testStepAfterName1).build(),
                    TestStep.Builder().withName(testStepAfterName2).build()
                )
            )
            .withStep(TestStep.Builder().withName("t1"))
            .build()[0]
    }
}