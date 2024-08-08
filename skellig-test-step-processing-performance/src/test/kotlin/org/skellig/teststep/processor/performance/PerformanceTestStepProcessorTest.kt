package org.skellig.teststep.processor.performance

import com.typesafe.config.ConfigFactory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.TestStepFactory
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processing.value.function.DefaultFunctionValueExecutor
import org.skellig.teststep.processing.value.property.DefaultPropertyExtractor
import org.skellig.teststep.processor.performance.config.PerformanceTestStepProcessorConfig
import org.skellig.teststep.processor.performance.exception.PerformanceTestStepException
import org.skellig.teststep.processor.performance.metrics.prometheus.MessageReceptionPrometheusMetric
import org.skellig.teststep.processor.performance.metrics.prometheus.RequestDurationPrometheusMetric
import org.skellig.teststep.processor.performance.model.LongRunResponse
import org.skellig.teststep.processor.performance.model.PerformanceTestStep
import java.time.LocalTime

class PerformanceTestStepProcessorTest {


    private lateinit var performanceTestStepProcessor: TestStepProcessor<PerformanceTestStep>
    private val testStepProcessor = mock<TestStepProcessor<TestStep>>()
    private val testStepFactory = mock<TestStepFactory<TestStep>>()

    @BeforeEach
    fun setUp() {
        init()
    }

    @Test
    fun `process simple performance test`() {
        val testStepToRun = mock<TestStep>()
        val testStep = PerformanceTestStep("t1", 1, LocalTime.of(0, 0, 1), listOf(), listOf(),
            listOf { _ -> testStepToRun })
        whenever(testStepProcessor.process(testStepToRun)).thenReturn(TestStepProcessor.TestStepRunResult(testStepToRun))

        performanceTestStepProcessor.process(testStep)
            .subscribe { _, _, e ->
                assertNull(e)
            }
    }

    @Test
    fun `process performance test with 2 runs and before and after runs`() {
        val testStepToRunBefore = mock<TestStep>()
        val testStepToRunAfter = mock<TestStep>()
        val testStepToRun1 = mock<TestStep>()
        val testStepToRun2 = mock<TestStep>()
        val testStep = PerformanceTestStep(
            "t1", 10, LocalTime.of(0, 0, 5),
            listOf { _ -> testStepToRunBefore },
            listOf { _ -> testStepToRunAfter },
            listOf({ _ -> testStepToRun1 }, { _ -> testStepToRun2 })
        )
        whenever(testStepToRunBefore.name).thenReturn("testStepToRunBefore")
        whenever(testStepToRunAfter.name).thenReturn("testStepToRunAfter")
        whenever(testStepToRun1.name).thenReturn("testStepToRun1")
        whenever(testStepToRun2.name).thenReturn("testStepToRun2")
        val testStepRunResult = TestStepProcessor.TestStepRunResult(mock())
        whenever(testStepProcessor.process(any())).thenReturn(testStepRunResult)
        testStepRunResult.notify("any", null)

        performanceTestStepProcessor.process(testStep)
            .subscribe { _, r, e ->
                assertNull(e)
                var log = ""
                ((r as LongRunResponse).getTimeSeries()["testStepToRunBefore"] as MessageReceptionPrometheusMetric).consumeTimeSeriesRecords { l -> log += l }
                (r.getTimeSeries()["t1"] as RequestDurationPrometheusMetric).consumeTimeSeriesRecords { l -> log += l }
                assertTrue(log.matches(Regex("\nName: testStepToRunBefore\nTotal requests: [\\d.]+\\nMPS: [\\d.]+\nName: t1")), "Log doesn't match")
            }
    }


    @Test
    fun `process performance test with 1 run and it has internal failure`() {
        val testStepToRunBefore = mock<TestStep>()
        val testStep = PerformanceTestStep(
            "t1", 10, LocalTime.of(0, 0, 1),
            listOf { _ -> testStepToRunBefore }, emptyList(), emptyList(),
        )
        whenever(testStepToRunBefore.name).thenReturn("testStepToRunBefore")
        val testStepRunResult = TestStepProcessor.TestStepRunResult(mock())
        whenever(testStepProcessor.process(any())).thenReturn(testStepRunResult)
        testStepRunResult.notify("any", RuntimeException("error"))

        performanceTestStepProcessor.process(testStep)
            .subscribe { _, r, e ->
                assertNull(e)
                var log = ""
                ((r as LongRunResponse).getTimeSeries()["testStepToRunBefore"] as MessageReceptionPrometheusMetric).consumeTimeSeriesRecords { l -> log += l }
                assertTrue(log.contains("Total requests: 1.0"), "Log contains invalid data")
            }
    }

    @Test
    fun `process performance test after runs have errors`() {
        val testStepToRunAfter1 = mock<TestStep>()
        val testStepToRunAfter2 = mock<TestStep>()
        val testStepToRun1 = mock<TestStep>()
        val testStep = PerformanceTestStep(
            "t1", 10, LocalTime.of(0, 0, 5),
            listOf(),
            listOf ({ _ -> testStepToRunAfter1 }, { _ -> testStepToRunAfter2 }),
            listOf { _ -> testStepToRun1 }
        )
        whenever(testStepToRunAfter1.name).thenReturn("testStepToRunAfter1")
        whenever(testStepToRunAfter2.name).thenReturn("testStepToRunAfter2")
        whenever(testStepToRun1.name).thenReturn("testStepToRun1")
        val testStepRunResult = TestStepProcessor.TestStepRunResult(mock())
        whenever(testStepProcessor.process(testStepToRun1)).thenReturn(testStepRunResult)
        testStepRunResult.notify("any", null)

        val testStepToRunAfter1Result = TestStepProcessor.TestStepRunResult(mock())
        testStepToRunAfter1Result.notify("any", RuntimeException("error 1"))
        val testStepToRunAfter2Result = TestStepProcessor.TestStepRunResult(mock())
        testStepToRunAfter2Result.notify("any", RuntimeException("error 2"))
        whenever(testStepProcessor.process(testStepToRunAfter1)).thenReturn(testStepToRunAfter1Result)
        whenever(testStepProcessor.process(testStepToRunAfter2)).thenReturn(testStepToRunAfter2Result)

        performanceTestStepProcessor.process(testStep)
            .subscribe { _, _, e ->
                assertNotNull(e)
                val errors = (e as PerformanceTestStepException).aggregate()
                assertEquals("Failed to run test step 'testStepToRunAfter1'", errors[0].message)
                assertEquals("Failed to run test step 'testStepToRunAfter2'", errors[1].message)
            }
    }

    @Test
    fun `process performance test when was closed`() {
        val testStep = PerformanceTestStep("t1", 1, LocalTime.of(0, 0, 1),
            listOf { _ -> mock<TestStep>() },
            listOf { _ -> mock<TestStep>() },
            listOf { _ -> mock<TestStep>() })

        performanceTestStepProcessor.close()
        performanceTestStepProcessor.process(testStep)
            .subscribe { _, _, e ->
                assertNull(e)
            }
    }

    @Test
    fun `get test step class`() {
        assertEquals(PerformanceTestStep::class.java, performanceTestStepProcessor.getTestStepClass())
    }

    private fun init() {
        val state = DefaultTestScenarioState()
        val config = PerformanceTestStepProcessorConfig().config(
            TestStepProcessorConfigDetails(
                state, ConfigFactory.load("performance-test.conf"), mock(),
                ValueExpressionContextFactory(
                    DefaultFunctionValueExecutor.Builder()
                        .withTestScenarioState(state)
                        .withClassLoader(this.javaClass.classLoader)
                        .build(), DefaultPropertyExtractor(null)
                ), testStepProcessor, testStepFactory
            )
        )
        performanceTestStepProcessor = config!!.testStepProcessor
    }
}