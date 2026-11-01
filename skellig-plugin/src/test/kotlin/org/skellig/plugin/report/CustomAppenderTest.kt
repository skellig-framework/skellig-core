package org.skellig.plugin.report

import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.log4j.spi.LocationInfo
import org.apache.log4j.spi.LoggingEvent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.processing.util.ScopeLogContext
import java.time.Instant
import java.time.LocalDateTime
import java.util.TimeZone
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CustomAppenderTest {

    @Test
    fun testLog() {
        val testStepLogger = Mockito.mock<TestStepLogger>()
        val event = createEvent("text")
        val eventTime =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(event.timeStamp), TimeZone.getDefault().toZoneId())

        CustomAppender(testStepLogger).doAppend(event)

        verify(testStepLogger).log(
            null,
            "${eventTime.format(CustomAppender.DATE_TIME_FORMAT)} ${event.getLevel()} " +
                    "${event.locationInformation.fullInfo} - ${event.message}"
        )
    }

    @Test
    fun testLogOfScope() {
        val testStepLogger = Mockito.mock<TestStepLogger>()
        val event = createEvent("text", "scope-1")
        val eventTime =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(event.timeStamp), TimeZone.getDefault().toZoneId())

        CustomAppender(testStepLogger).doAppend(event)

        verify(testStepLogger).log(
            "scope-1",
            "${eventTime.format(CustomAppender.DATE_TIME_FORMAT)} ${event.getLevel()} " +
                    "${event.locationInformation.fullInfo} - text"
        )
    }

    @Test
    fun testLogOfScopeWhenMessageStartsWithAnotherScopeId() {
        val testStepLogger = Mockito.mock<TestStepLogger>()

        CustomAppender(testStepLogger).doAppend(createEvent("[scope-2]: text", "scope-1"))

        verify(testStepLogger).log(Mockito.eq("scope-1"), Mockito.anyString())
    }

    @Test
    fun testLogWhenMessageStartsWithScopeIdButThereIsNoScopeInLogContext() {
        val testStepLogger = Mockito.mock<TestStepLogger>()

        CustomAppender(testStepLogger).doAppend(createEvent("[scope-1]: text"))

        verify(testStepLogger).log(Mockito.isNull(), Mockito.anyString())
    }

    @Test
    fun testClose() {
        val testStepLogger = Mockito.mock<TestStepLogger>()
        CustomAppender(testStepLogger).close()

        verify(testStepLogger).clear()
    }

    @Test
    fun testRequiresLayout() {
        // false by default
        assertFalse(CustomAppender(Mockito.mock()).requiresLayout())
    }

    @Test
    fun testScopesLoggingAtTheSameTimeCollectOnlyTheirOwnLinesWhateverTheMessageIs() {
        val testStepLogger = DefaultTestStepLogger()
        val logger = Logger.getLogger("CustomAppenderTest.testScopesLoggingAtTheSameTime")
        val appender = CustomAppender(testStepLogger)
        logger.additivity = false
        logger.level = Level.INFO
        logger.addAppender(appender)
        val scopeIds = List(6) { "scope$it" }
        scopeIds.forEach { testStepLogger.clear(it) }
        val linesPerScope = 200
        val startSignal = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(scopeIds.size)

        try {
            val futures = scopeIds.map { scopeId ->
                executor.submit {
                    startSignal.await()
                    // the lines have no scope id in the message, as the ones logged by user code or libraries
                    ScopeLogContext.runWith(TestScenarioScope(scopeId, DefaultTestScenarioState())) {
                        repeat(linesPerScope) { logger.info("line $it") }
                    }
                }
            }
            startSignal.countDown()
            futures.forEach { it.get(30, TimeUnit.SECONDS) }
        } finally {
            executor.shutdownNow()
            logger.removeAppender(appender)
        }

        scopeIds.forEach { scopeId ->
            val lines = testStepLogger.getLogsAndClean(scopeId)
            assertEquals(linesPerScope, lines.size, "Lines of $scopeId")
            lines.forEachIndexed { index, line ->
                assertTrue(line.endsWith(" - line $index"), "Line of $scopeId: $line")
            }
        }
    }

    private fun createEvent(message: String, scopeId: String? = null): LoggingEvent {
        val event = Mockito.mock<LoggingEvent>()
        scopeId?.let { whenever(event.getMDC(ScopeLogContext.KEY)).thenReturn(it) }
        whenever(event.getTimeStamp()).thenReturn(System.currentTimeMillis())
        whenever(event.message).thenReturn(message)
        whenever(event.locationInformation).thenReturn(LocationInfo("file", "classname", "method", "line"))
        return event
    }
}
