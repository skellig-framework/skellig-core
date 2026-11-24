package org.skellig.teststep.processing.processor

import org.apache.log4j.AppenderSkeleton
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.log4j.spi.LoggingEvent
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.state.TestScenarioScope
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ScopeAwareProcessorTest {
    private val warnings = CopyOnWriteArrayList<String>()
    private val logger = Logger.getLogger(CompositeTestStepProcessor::class.java)
    private val previousLevel = logger.level
    private val appender = object : AppenderSkeleton() {
        override fun append(event: LoggingEvent) {
            if (event.level == Level.WARN) warnings.add(event.renderedMessage)
        }
        override fun close() {}
        override fun requiresLayout() = false
    }

    @BeforeEach
    fun captureWarnings() {
        logger.level = Level.WARN
        logger.addAppender(appender)
    }

    @AfterEach
    fun stopCapturingWarnings() {
        logger.removeAppender(appender)
        logger.level = previousLevel
    }

    @Test
    fun `unmarked processor warns once when handling scoped steps in parallel mode`() {
        processor(true).use { composite ->
            composite.registerTestStepProcessor(UnmarkedProcessor())
            repeat(2) { composite.process(Step()) }
        }
        assertEquals(1, warnings.size)
        assertTrue(warnings.single().contains(UnmarkedProcessor::class.java.name))
        assertTrue(warnings.single().contains("ScopeAware"))
    }

    @Test
    fun `marked processor does not warn`() {
        processor(true).use { composite ->
            composite.registerTestStepProcessor(MarkedProcessor())
            composite.process(Step())
        }
        assertTrue(warnings.isEmpty())
    }

    @Test
    fun `disabled parallel execution does not warn`() {
        processor().use { composite ->
            composite.registerTestStepProcessor(UnmarkedProcessor())
            composite.process(Step())
        }
        assertTrue(warnings.isEmpty())
    }

    @Test
    fun `unscoped step does not consume the warning`() {
        processor(true).use { composite ->
            composite.registerTestStepProcessor(UnmarkedProcessor())
            composite.process(Step(null))
            assertTrue(warnings.isEmpty())
            composite.process(Step())
            assertEquals(1, warnings.size)
        }
    }

    @Test
    fun `concurrent scoped steps warn only once`() {
        processor(true).use { composite ->
            composite.registerTestStepProcessor(UnmarkedProcessor())
            val pool = Executors.newFixedThreadPool(8)
            val ready = CountDownLatch(8)
            val start = CountDownLatch(1)
            try {
                val results = (1..8).map {
                    pool.submit {
                        ready.countDown()
                        check(start.await(10, TimeUnit.SECONDS))
                        repeat(20) { composite.process(Step()) }
                    }
                }
                assertTrue(ready.await(10, TimeUnit.SECONDS))
                start.countDown()
                results.forEach { it.get(10, TimeUnit.SECONDS) }
            } finally {
                start.countDown()
                pool.shutdownNow()
            }
        }
        assertEquals(1, warnings.size)
    }

    @Test
    fun `each context warns independently`() {
        repeat(2) {
            processor(true).use { composite ->
                composite.registerTestStepProcessor(UnmarkedProcessor())
                composite.process(Step())
            }
        }
        assertEquals(2, warnings.size)
    }

    @Test
    fun `warning is per processor class rather than per instance`() {
        processor(true).use { composite ->
            composite.registerTestStepProcessor(UnmarkedProcessor())
            composite.registerTestStepProcessor(UnmarkedProcessor(OtherStep::class.java))
            composite.process(Step())
            composite.process(OtherStep())
        }
        assertEquals(1, warnings.size)
    }

    @Test
    fun `different unmarked processor classes each warn`() {
        processor(true).use { composite ->
            composite.registerTestStepProcessor(UnmarkedProcessor())
            composite.registerTestStepProcessor(OtherProcessor())
            repeat(2) {
                composite.process(Step())
                composite.process(OtherStep())
            }
        }
        assertEquals(2, warnings.size)
        assertTrue(warnings.any { it.contains(UnmarkedProcessor::class.java.name) })
        assertTrue(warnings.any { it.contains(OtherProcessor::class.java.name) })
    }

    private class OtherStep : Step()
    private class OtherProcessor : UnmarkedProcessor(OtherStep::class.java)

    private class MarkedProcessor : UnmarkedProcessor(), ScopeAware

    private fun processor(parallel: Boolean = false) = CompositeTestStepProcessor.Builder()
        .withValueConvertDelegate { _, _ -> null }
        .withProcessTestStepDelegate { _, _ -> TestStepProcessor.TestStepRunResult(null) }
        .withParallelExecution(parallel)
        .build()

    private open class Step(override val scope: TestScenarioScope? = TestScenarioScope("test", DefaultTestScenarioState())) : TestStep {
        override val name = "custom step"
    }

    private open class UnmarkedProcessor(private val stepClass: Class<out Step> = Step::class.java) : TestStepProcessor<Step> {
        override fun getTestStepClass() = stepClass
        override fun process(testStep: Step) = TestStepProcessor.TestStepRunResult(testStep)
    }
}
