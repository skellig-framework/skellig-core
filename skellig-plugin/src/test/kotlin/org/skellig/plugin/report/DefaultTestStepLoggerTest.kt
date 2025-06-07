package org.skellig.plugin.report

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultTestStepLoggerTest {

    private lateinit var testStepLogger: DefaultTestStepLogger

    @BeforeEach
    fun setUp() {
        testStepLogger = DefaultTestStepLogger()
    }

    @Test
    fun `test log adds text to log records`() {
        // when
        testStepLogger.log("Test message")

        // then
        val logs = testStepLogger.getLogsAndClean()
        assertEquals(1, logs.size)
        assertEquals("Test message", logs[0])
    }

    @Test
    fun `test log adds multiple messages to log records`() {
        // when
        testStepLogger.log("Message 1")
        testStepLogger.log("Message 2")
        testStepLogger.log("Message 3")

        // then
        val logs = testStepLogger.getLogsAndClean()
        assertEquals(3, logs.size)
        assertEquals("Message 1", logs[0])
        assertEquals("Message 2", logs[1])
        assertEquals("Message 3", logs[2])
    }

    @Test
    fun `test getLogsAndClean returns copy of logs and clears original`() {
        // given
        testStepLogger.log("Message 1")
        testStepLogger.log("Message 2")

        // when
        val logs = testStepLogger.getLogsAndClean()

        // then
        assertEquals(2, logs.size)
        assertEquals("Message 1", logs[0])
        assertEquals("Message 2", logs[1])

        // verify logs were cleared
        val emptyLogs = testStepLogger.getLogsAndClean()
        assertTrue(emptyLogs.isEmpty())
    }

    @Test
    fun `test getLogsAndClean returns empty list when no logs`() {
        // when
        val logs = testStepLogger.getLogsAndClean()

        // then
        assertTrue(logs.isEmpty())
    }

    @Test
    fun `test clear removes all log records`() {
        // given
        testStepLogger.log("Message 1")
        testStepLogger.log("Message 2")

        // when
        testStepLogger.clear()

        // then
        val logs = testStepLogger.getLogsAndClean()
        assertTrue(logs.isEmpty())
    }

    @Test
    fun `test clear does nothing when log records already empty`() {
        // when
        testStepLogger.clear()

        // then
        val logs = testStepLogger.getLogsAndClean()
        assertTrue(logs.isEmpty())
    }

    @Test
    fun `test that log records order is preserved`() {
        // given
        val expectedMessages = List(100) { i -> "Message $i" }

        // when
        expectedMessages.forEach { testStepLogger.log(it) }

        // then
        val logs = testStepLogger.getLogsAndClean()
        assertEquals(expectedMessages.size, logs.size)
        expectedMessages.forEachIndexed { index, message ->
            assertEquals(message, logs[index])
        }
    }
}
