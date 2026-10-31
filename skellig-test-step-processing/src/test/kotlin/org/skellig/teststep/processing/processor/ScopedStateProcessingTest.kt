package org.skellig.teststep.processing.processor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.verify
import org.mockito.kotlin.mock
import org.skellig.teststep.processing.model.ClassTestStep
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.ScenarioStateUpdater
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.state.TestScenarioScope
import java.util.regex.Pattern

/**
 * A processor reads and writes only the state of the scope of the test step, and fails
 * when the test step has no scope.
 */
class ScopedStateProcessingTest {

    private val scopeState = DefaultTestScenarioState()
    private val scope = TestScenarioScope("s1", scopeState)
    private val otherState = DefaultTestScenarioState()

    @Nested
    inner class DefaultTestStepProcessing {

        private val processor = DefaultTestStepProcessor.Builder().build()

        @Test
        fun testStoresTestStepInStateOfItsScope() {
            val testStep = testStep("t1", scope)

            processor.process(testStep)

            assertAll(
                { assertSame(testStep, scopeState.get("t1")) },
                { assertNull(otherState.get("t1")) },
            )
        }

        @Test
        fun testFailsWhenNoScope() {
            val ex = assertThrows<IllegalStateException> { processor.process(testStep("t1")) }

            assertEquals("Test step 't1' has no scenario scope. A test step must be run with a TestScenarioScope", ex.message)
        }
    }

    @Nested
    inner class BaseTestStepProcessing {

        private val processor = BaseTestStepProcessorTest.BaseTestStepProcessorForTest()

        @Test
        fun testStoresTestStepAndResultInStateOfItsScope() {
            val testStep = testStep("t1", scope)

            processor.process(testStep)

            assertAll(
                { assertSame(testStep, scopeState.get("t1")) },
                { assertEquals("processed", scopeState.get("t1_result")) },
                { assertNull(otherState.get("t1")) },
                { assertNull(otherState.get("t1_result")) },
            )
        }

        @Test
        fun testUpdatesStateOfItsScopeWithScenarioStateUpdaters() {
            val updater = mock<ScenarioStateUpdater>()
            val testStep = DefaultTestStep.DefaultTestStepBuilder()
                .withName("t1")
                .withScenarioStateUpdater(listOf(updater))
                .withScope(scope)
                .build()

            processor.process(testStep)

            verify(updater).update("processed", scopeState)
        }

        @Test
        fun testFailsWhenNoScope() {
            assertThrows<IllegalStateException> { processor.process(testStep("t1")) }
        }
    }

    @Nested
    inner class ClassTestStepProcessing {

        private val processor = ClassTestStepProcessor()

        @Test
        fun testStoresResultInStateOfItsScope() {
            val testStep = classTestStep(mapOf(TestScenarioScope.PARAMETER_KEY to scope))

            processor.process(testStep)

            assertAll(
                { assertEquals("done", scopeState.get("c1_result")) },
                { assertNull(otherState.get("c1_result")) },
            )
        }

        @Test
        fun testFailsWhenNoScope() {
            assertThrows<IllegalStateException> { processor.process(classTestStep(emptyMap())) }
        }

        @Test
        fun testMethodParametersMapDoesNotContainTheScope() {
            val testStep = ClassTestStep(
                "c2", Pattern.compile("run"), ScopedSteps::class.java,
                ScopedSteps::class.java.getMethod("runWithMap", Map::class.java), "run",
                mapOf("a" to "b", TestScenarioScope.PARAMETER_KEY to scope)
            )

            processor.process(testStep)

            assertEquals(mapOf("a" to "b"), scope.parallelGroup.instanceOf(ScopedSteps::class.java).receivedParameters)
        }

        private fun classTestStep(parameters: Map<String, Any?>) = ClassTestStep(
            "c1", Pattern.compile("run"), ScopedSteps::class.java,
            ScopedSteps::class.java.getMethod("run"), "run", parameters
        )
    }

    private fun testStep(id: String, scope: TestScenarioScope? = null) =
        DefaultTestStep.DefaultTestStepBuilder().withId(id).withName(id).withScope(scope).build()
}

class ScopedSteps {

    var receivedParameters: Map<*, *>? = null

    fun run() = "done"

    fun runWithMap(parameters: Map<*, *>) {
        receivedParameters = parameters
    }
}
