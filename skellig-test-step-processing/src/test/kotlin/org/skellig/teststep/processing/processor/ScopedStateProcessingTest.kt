package org.skellig.teststep.processing.processor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.mockito.Mockito.verify
import org.mockito.kotlin.mock
import org.skellig.teststep.processing.model.ClassTestStep
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.ScenarioStateUpdater
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.state.TestScenarioScope
import java.util.regex.Pattern

/**
 * A processor reads and writes the state of the scope of the test step, and only uses its own state
 * when the test step has no scope.
 */
class ScopedStateProcessingTest {

    private val defaultState = DefaultTestScenarioState()
    private val scopeState = DefaultTestScenarioState()
    private val scope = TestScenarioScope("s1", scopeState)

    @Nested
    inner class DefaultTestStepProcessing {

        private val processor = DefaultTestStepProcessor.Builder().withTestScenarioState(defaultState).build()

        @Test
        fun testStoresTestStepInStateOfItsScope() {
            val testStep = testStep("t1").also { it.scope = scope }

            processor.process(testStep)

            assertAll(
                { assertSame(testStep, scopeState.get("t1")) },
                { assertNull(defaultState.get("t1")) },
            )
        }

        @Test
        fun testStoresTestStepInDefaultStateWhenNoScope() {
            val testStep = testStep("t1")

            processor.process(testStep)

            assertSame(testStep, defaultState.get("t1"))
        }
    }

    @Nested
    inner class BaseTestStepProcessing {

        private val processor = BaseTestStepProcessorTest.BaseTestStepProcessorForTest(defaultState)

        @Test
        fun testStoresTestStepAndResultInStateOfItsScope() {
            val testStep = testStep("t1").also { it.scope = scope }

            processor.process(testStep)

            assertAll(
                { assertSame(testStep, scopeState.get("t1")) },
                { assertEquals("processed", scopeState.get("t1_result")) },
                { assertNull(defaultState.get("t1")) },
                { assertNull(defaultState.get("t1_result")) },
            )
        }

        @Test
        fun testUpdatesStateOfItsScopeWithScenarioStateUpdaters() {
            val updater = mock<ScenarioStateUpdater>()
            val testStep = DefaultTestStep.DefaultTestStepBuilder()
                .withName("t1")
                .withScenarioStateUpdater(listOf(updater))
                .build()
                .also { it.scope = scope }

            processor.process(testStep)

            verify(updater).update("processed", scopeState)
        }

        @Test
        fun testStoresTestStepAndResultInDefaultStateWhenNoScope() {
            val testStep = testStep("t1")

            processor.process(testStep)

            assertAll(
                { assertSame(testStep, defaultState.get("t1")) },
                { assertEquals("processed", defaultState.get("t1_result")) },
            )
        }
    }

    @Nested
    inner class ClassTestStepProcessing {

        private val processor = ClassTestStepProcessor(defaultState)

        @Test
        fun testStoresResultInStateOfItsScope() {
            val testStep = classTestStep(mapOf(TestScenarioScope.PARAMETER_KEY to scope))

            processor.process(testStep)

            assertAll(
                { assertEquals("done", scopeState.get("c1_result")) },
                { assertNull(defaultState.get("c1_result")) },
            )
        }

        @Test
        fun testStoresResultInDefaultStateWhenNoScope() {
            processor.process(classTestStep(emptyMap()))

            assertEquals("done", defaultState.get("c1_result"))
        }

        @Test
        fun testMethodParametersMapDoesNotContainTheScope() {
            receivedParameters = null
            val testStep = ClassTestStep(
                "c2", Pattern.compile("run"), this@ClassTestStepProcessing,
                ClassTestStepProcessing::class.java.getMethod("runWithMap", Map::class.java), "run",
                mapOf("a" to "b", TestScenarioScope.PARAMETER_KEY to scope)
            )

            processor.process(testStep)

            assertEquals(mapOf("a" to "b"), receivedParameters)
        }

        private fun classTestStep(parameters: Map<String, Any?>) = ClassTestStep(
            "c1", Pattern.compile("run"), this@ClassTestStepProcessing,
            ClassTestStepProcessing::class.java.getMethod("run"), "run", parameters
        )

        private var receivedParameters: Map<*, *>? = null

        @Suppress("unused")
        fun run() = "done"

        @Suppress("unused")
        fun runWithMap(parameters: Map<*, *>) {
            receivedParameters = parameters
        }
    }

    private fun testStep(id: String) = DefaultTestStep.DefaultTestStepBuilder().withId(id).withName(id).build()
}
