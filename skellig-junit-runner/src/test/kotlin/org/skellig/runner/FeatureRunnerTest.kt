package org.skellig.runner

import org.junit.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.runner.notification.RunNotifier
import org.junit.runner.notification.StoppedByUserException
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.kotlin.*
import org.skellig.feature.Feature
import org.skellig.feature.TestScenario
import org.skellig.feature.TestStep
import org.skellig.feature.event.*
import org.skellig.feature.hook.SkelligHookRunner
import org.skellig.feature.hook.annotation.AfterTestFeature
import org.skellig.feature.hook.annotation.BeforeTestFeature
import org.skellig.feature.hook.annotation.BeforeTestScenario
import org.skellig.feature.metadata.TagsFilter
import org.skellig.plugin.report.TestStepLogger
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.PropertyFormatUtils
import org.skellig.teststep.runner.TestStepRunner


class FeatureRunnerTest {

    private val testScenarioState = mock<TestScenarioState>()
    private val feature = createFeature()
    private val hookRunner = mock<SkelligHookRunner>()
    private val testStepRunner = mock<TestStepRunner>()
    private val testStepLogger = mock<TestStepLogger>()
    private val eventDispatcher = mock<SkelligTestEventDispatcher>()
    private val featureRunner = FeatureRunner.create(
        feature, testStepRunner, testScenarioState, testStepLogger, hookRunner, eventDispatcher, TagsFilter(emptySet(), emptySet())
    )

    @Test
    fun testRunAndVerifyCorrectNotificationsAndStateClean() {
        val notifier = mock<RunNotifier>()
        featureRunner.run(notifier)

        assertAll(
            { verify(notifier).fireTestSuiteStarted(argThat { o -> o.displayName == feature.scenarios!![0].name }) },
            { verify(notifier).fireTestSuiteFinished(argThat { o -> o.displayName == feature.scenarios!![0].name }) },
            { verify(testScenarioState).clean() }
        )
    }

    @Test
    fun testRunAndVerifyLogIsAttached() {
        val expectedLogRecords = listOf("log record 1")
        whenever(testStepLogger.getLogsAndClean()).thenReturn(expectedLogRecords)

        featureRunner.run(mock<RunNotifier>())

        verify(eventDispatcher).dispatch(argThat { e -> e is TestStepFinishedEvent && e.logRecords == expectedLogRecords })
    }

    @Test
    fun testRunAndVerifyLogIsAttachedWhenTestFailed() {
        doAnswer {
            val testStepRunResult = TestStepProcessor.TestStepRunResult(mock())
            testStepRunResult.notify(null, RuntimeException("Error"))
            testStepRunResult
        }.whenever(testStepRunner).run(feature.scenarios!![0].steps!![0].name, emptyMap())

        val expectedLogRecords = listOf("log record 1")
        whenever(testStepLogger.getLogsAndClean()).thenReturn(expectedLogRecords)

        featureRunner.run(mock<RunNotifier>())

        verify(eventDispatcher).dispatch(argThat { e ->
            e is TestStepFinishedEvent && e.logRecords == expectedLogRecords
        })
        verify(eventDispatcher).dispatch(argThat { e ->
            e is TestStepProcessingFinishedEvent && e.result.errorLog != null
        })
    }

    @Test
    fun testRunAndVerifyReportHasAllData() {
        val expectedResponse = listOf(mapOf(Pair("a", "1010")))
        val testStep = mock<org.skellig.teststep.processing.model.TestStep>()
        whenever(testStep.getFullInfo()).thenReturn(mock())

        val testStepRunResult = TestStepProcessor.TestStepRunResult(testStep)
        whenever(testStepRunner.run(feature.scenarios!![0].steps!![0].name, emptyMap())).thenReturn(testStepRunResult)

        featureRunner.run(mock<RunNotifier>())
        testStepRunResult.notify(expectedResponse, null)

        verify(eventDispatcher).dispatch(argThat { e ->
            e is TestStepStartedEvent && e.testStep.name == feature.scenarios!![0].steps!![0].name
        })
        verify(eventDispatcher).dispatch(argThat { e ->
            e is TestStepProcessingFinishedEvent && e.result.errorLog == null &&
                    e.testStepInfo == testStep.getFullInfo() &&
                    e.result.duration > 0 &&
                    e.result.result == PropertyFormatUtils.toString(expectedResponse, 0)
        })
    }

    @Test
    fun testRunWhenTestStepFailsAndVerifyReportHasCorrectData() {
        val testStepRunResult = TestStepProcessor.TestStepRunResult(mock())
        whenever(testStepRunner.run(feature.scenarios!![0].steps!![0].name, emptyMap())).thenReturn(testStepRunResult)

        featureRunner.run(mock<RunNotifier>())
        assertThrows<RuntimeException> { testStepRunResult.notify(null, RuntimeException("Error")) }

        verify(eventDispatcher).dispatch(argThat { e -> e is TestStepProcessingFinishedEvent && e.result.errorLog?.startsWith("java.lang.RuntimeException: Error") == true })
    }

    @Test
    fun testRunWhenTestStepFailsAndVerifySecondOneIsIgnored() {
        val testStepRunResult = TestStepProcessor.TestStepRunResult(mock())
        doAnswer {
            testStepRunResult.notify(null, RuntimeException("Error"))
            testStepRunResult
        }.whenever(testStepRunner).run(feature.scenarios!![0].steps!![0].name, emptyMap())

        val notifier = mock<RunNotifier>()
        featureRunner.run(notifier)

        verify(notifier).fireTestIgnored(argThat { d ->
            d.displayName == "${feature.scenarios!![0].steps!![1].name}(${feature.scenarios!![0].name})"
        })
    }

    @Test
    fun testRunAndVerifyHookOrderRunIsCorrect() {
        featureRunner.run(mock<RunNotifier>())

        val orderVerifier = Mockito.inOrder(hookRunner)
        orderVerifier.verify(hookRunner).run(eq(feature.tags), eq(BeforeTestFeature::class.java), argThat { true })
        orderVerifier.verify(hookRunner).run(eq(feature.tags), eq(AfterTestFeature::class.java), argThat { true })
    }

    @Test
    fun testRunAndVerifyHookReportIsAdded() {
        val expectedBeforeMethodName = "mockBeforeHook"
        val expectedAfterMethodName = "mockAfterHook"
        val expectedBeforeDuration: Long = 10
        val expectedAfterDuration: Long = 100
        doAnswer {
            it.getArgument<(String, Throwable?, Long) -> Unit>(2).invoke(expectedBeforeMethodName, null, expectedBeforeDuration)
        }.whenever(hookRunner).run(eq(feature.tags), eq(BeforeTestFeature::class.java), argThat { true })

        doAnswer {
            it.getArgument<(String, Throwable?, Long) -> Unit>(2).invoke(expectedAfterMethodName, null, expectedAfterDuration)
        }.whenever(hookRunner).run(eq(feature.tags), eq(AfterTestFeature::class.java), argThat { true })

        featureRunner.run(mock<RunNotifier>())

        assertAll(
            {
                verify(eventDispatcher).dispatch(argThat { e ->
                    e is HookFinishedEvent && e.methodName == expectedBeforeMethodName &&
                            e.result.errorLog == null &&
                            e.result.duration == expectedBeforeDuration
                })
            },

            {
                verify(eventDispatcher).dispatch(argThat { e ->
                    e is HookFinishedEvent && e.methodName == expectedAfterMethodName &&
                            e.result.errorLog == null &&
                            e.result.duration == expectedAfterDuration
                })
            }
        )
    }

    @Test
    fun testRunWhenBeforeFeatureHookFailedThenVerifyFailureNotification() {
        doAnswer {
            it.getArgument<(String, Throwable?, Long) -> Unit>(2).invoke("m1", RuntimeException("error"), 0)
        }.whenever(hookRunner).run(eq(feature.tags), eq(BeforeTestFeature::class.java), argThat { true })

        val notifier = mock<RunNotifier>()
        featureRunner.run(notifier)

        verify(eventDispatcher).dispatch(argThat { e -> e is HookFinishedEvent && e.result.errorLog?.contains("error") == true })
        verify(notifier).fireTestFailure(argThat { i -> i.description.displayName == feature.name })
    }

    @Test
    fun testRunWhenBeforeScenarioHookFailedThenVerifyFailureNotification() {
        val notifier = mock<RunNotifier>()
        featureRunner.run(notifier)

        doThrow(RuntimeException("scenario error"))
            .whenever(hookRunner).run(eq(feature.tags), eq(BeforeTestScenario::class.java), argThat { true })

        featureRunner.run(notifier)

        verify(notifier).fireTestFailure(argThat { i -> i.description.displayName == feature.scenarios!![0].name })
    }

    @Test
    fun testRunAndVerifyFeatureBeforeAndAfterTestStepsRun() {
        val feature = createFeatureWithBeforeAndAfterSteps()
        val featureRunner = FeatureRunner(
            feature, testScenarioState, TagsFilter(emptySet(), emptySet()),
            hookRunner, testStepRunner, mock<TestStepLogger>(), eventDispatcher
        )
        val result1 = mock<TestStepProcessor.TestStepRunResult>()
        val result2 = mock<TestStepProcessor.TestStepRunResult>()
        val result3 = mock<TestStepProcessor.TestStepRunResult>()
        val result4 = mock<TestStepProcessor.TestStepRunResult>()
        whenever(testStepRunner.run(feature.beforeSteps!![0].name, emptyMap())).thenReturn(result1)
        whenever(testStepRunner.run(feature.beforeSteps!![1].name, emptyMap())).thenReturn(result2)
        whenever(testStepRunner.run(feature.afterSteps!![0].name, emptyMap())).thenReturn(result3)
        whenever(testStepRunner.run(feature.afterSteps!![1].name, emptyMap())).thenReturn(result4)

        val notifier = mock<RunNotifier>()
        featureRunner.run(notifier)

        val orderVerifier = Mockito.inOrder(notifier)
        orderVerifier.verify(notifier, times(1)).fireTestStarted(argThat { o -> o.displayName == "${feature.beforeSteps!![0].name}(Before Feature)" })
        orderVerifier.verify(notifier, times(1)).fireTestStarted(argThat { o -> o.displayName == "${feature.beforeSteps!![1].name}(Before Feature)" })
        orderVerifier.verify(notifier, times(1)).fireTestStarted(argThat { o -> o.displayName == "${feature.afterSteps!![0].name}(After Feature)" })
        orderVerifier.verify(notifier, times(1)).fireTestStarted(argThat { o -> o.displayName == "${feature.afterSteps!![1].name}(After Feature)" })

        assertAll(
            { verify(result1).subscribe(argThat { true }) },
            { verify(result2).subscribe(argThat { true }) },
            { verify(result3).subscribe(argThat { true }) },
            { verify(result4).subscribe(argThat { true }) },
            { verify(result1).awaitResult() },
            { verify(result2).awaitResult() },
            { verify(result3).awaitResult() },
            { verify(result4).awaitResult() }
        )
    }

    @Test
    fun testRunWhenBeforeStepFailsOnWaitingResult() {
        val feature = createFeatureWithBeforeAndAfterSteps()
        val featureRunner = FeatureRunner(
            feature, testScenarioState, TagsFilter(emptySet(), emptySet()),
            hookRunner, testStepRunner, mock<TestStepLogger>(), eventDispatcher
        )

        val result1 = mock<TestStepProcessor.TestStepRunResult>()
        val result2 = mock<TestStepProcessor.TestStepRunResult>()
        val result3 = mock<TestStepProcessor.TestStepRunResult>()
        val result4 = mock<TestStepProcessor.TestStepRunResult>()
        whenever(testStepRunner.run(feature.beforeSteps!![0].name, emptyMap())).thenReturn(result1)
        whenever(testStepRunner.run(feature.beforeSteps!![1].name, emptyMap())).thenReturn(result2)
        whenever(testStepRunner.run(feature.afterSteps!![0].name, emptyMap())).thenReturn(result3)
        whenever(testStepRunner.run(feature.afterSteps!![1].name, emptyMap())).thenReturn(result4)

        whenever(result1.awaitResult()).thenThrow(RuntimeException("internal error"))
        whenever(result4.awaitResult()).thenThrow(RuntimeException("internal error 2"))

        val notifier = mock<RunNotifier>()
        featureRunner.run(notifier)

        assertAll(
            { verify(result1).awaitResult() },
            { verify(result2).awaitResult() },
            { verify(result3).awaitResult() },
            { verify(result4).awaitResult() },
            { verify(notifier).fireTestFailure(argThat { i -> i.description.displayName == "Before Feature" }) },
            { verify(notifier).fireTestFailure(argThat { i -> i.description.displayName == "After Feature" }) },
            { verify(notifier).fireTestFailure(argThat { i -> i.message == "internal error" }) },
            { verify(notifier).fireTestFailure(argThat { i -> i.message == "internal error 2" }) }
        )
    }

    @Test
    fun testRunWhenTestStepFailsOnWaitingResult() {
        val result1 = mock<TestStepProcessor.TestStepRunResult>()
        whenever(testStepRunner.run(feature.scenarios!![0].steps!![0].name, emptyMap())).thenReturn(result1)
        whenever(result1.awaitResult()).thenThrow(RuntimeException("internal error"))

        val notifier = mock<RunNotifier>()
        featureRunner.run(notifier)

        verify(notifier).fireTestFailure(argThat { i -> i.message == "internal error" })
    }

    @Test
    fun testRunWhenTestStepFailsOnUserInterrupt() {
        doThrow(StoppedByUserException())
            .whenever(testStepRunner).run(feature.scenarios!![0].steps!![0].name, emptyMap())

        org.junit.jupiter.api.assertThrows<StoppedByUserException> { featureRunner.run(mock<RunNotifier>()) }
    }

    private fun createFeatureWithBeforeAndAfterSteps(): Feature {
        val testStepBeforeName1 = "test step before 1"
        val testStepBeforeName2 = "test step before 2"
        val testStepAfterName1 = "test step after 1"
        val testStepAfterName2 = "test step after 2"

        return Feature.Builder()
            .withName("f1")
            .withFilePath("/path/f1.sf")
            .withBeforeFeatureStep(TestStep.Builder().withName(testStepBeforeName1))
            .withBeforeFeatureStep(TestStep.Builder().withName(testStepBeforeName2))
            .withAfterFeatureStep(TestStep.Builder().withName(testStepAfterName1))
            .withAfterFeatureStep(TestStep.Builder().withName(testStepAfterName2))
            .build()
    }


    private fun createFeature(): Feature {
        return Feature.Builder()
            .withName("f1")
            .withFilePath("/path/f1.sf")
            .withTestScenario(
                TestScenario.Builder()
                    .withName("s1")
                    .withStep(TestStep.Builder().withName("test step 1"))
                    .withStep(TestStep.Builder().withName("test step 2"))
            ).build()
    }
}