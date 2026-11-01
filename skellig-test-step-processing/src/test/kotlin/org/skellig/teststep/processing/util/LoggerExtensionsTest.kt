package org.skellig.teststep.processing.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.processing.state.TestScenarioState
import org.slf4j.Logger

@DisplayName("Log of test step")
class LoggerExtensionsTest {

    private val logger = mock<Logger>()
    private val testStepOfScope = DefaultTestStep(
        name = "step 1",
        scope = TestScenarioScope("scope-1", mock<TestScenarioState>())
    )
    private val testStepWithoutScope = DefaultTestStep(name = "step 2")

    @Test
    @DisplayName("When info of test step Then message is prefixed with the scope id")
    fun testInfoIsPrefixedWithScopeId() {
        logger.info(testStepOfScope, "message")

        verify(logger).info("[scope-1]: message")
    }

    @Test
    @DisplayName("When info of test step without scope Then message is logged as it is")
    fun testInfoWithoutScope() {
        logger.info(testStepWithoutScope, "message")

        verify(logger).info("message")
    }

    @Test
    @DisplayName("When error of test step Then message is prefixed with the scope id")
    fun testErrorIsPrefixedWithScopeId() {
        logger.error(testStepOfScope, "message")

        assertEquals(listOf("[scope-1]: message"), loggedMessages())
    }

    @Test
    @DisplayName("When error of test step without scope Then message is logged as it is")
    fun testErrorWithoutScope() {
        logger.error(testStepWithoutScope, "message")

        assertEquals(listOf("message"), loggedMessages())
    }

    @Test
    @DisplayName("When debug of test step Then message is prefixed with the scope id")
    fun testDebugIsPrefixedWithScopeId() {
        whenever(logger.isDebugEnabled).thenReturn(true)

        logger.debug(testStepOfScope) { "message" }

        verify(logger).debug("[scope-1]: message")
    }

    @Test
    @DisplayName("When debug of test step without scope Then message is logged as it is")
    fun testDebugWithoutScope() {
        whenever(logger.isDebugEnabled).thenReturn(true)

        logger.debug(testStepWithoutScope) { "message" }

        verify(logger).debug("message")
    }

    @Test
    @DisplayName("When debug is disabled Then message of test step is not built")
    fun testDebugMessageIsNotBuiltWhenDisabled() {
        whenever(logger.isDebugEnabled).thenReturn(false)

        logger.debug(testStepOfScope) { error("The message must not be built") }

        verify(logger, never()).debug(any<String>())
    }

    /**
     * The messages of the calls of any log level, as the level of the call is not what these tests are about.
     */
    private fun loggedMessages(): List<String> =
        Mockito.mockingDetails(logger).invocations
            .filter { it.method.name in setOf("info", "error", "debug") }
            .map { it.arguments[0] as String }
}
