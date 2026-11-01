package org.skellig.plugin.report

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DefaultTestStepLoggerTest {

    private val testStepLogger = DefaultTestStepLogger()

    @Test
    fun `test log adds text to log records of the scope`() {
        // given
        testStepLogger.clear("scope1")

        // when
        testStepLogger.log("scope1", "Test message")

        // then
        assertEquals(listOf("Test message"), testStepLogger.getLogsAndClean("scope1"))
    }

    @Test
    fun `test log adds multiple messages to log records`() {
        // given
        testStepLogger.clear("scope1")

        // when
        testStepLogger.log("scope1", "Message 1")
        testStepLogger.log("scope1", "Message 2")
        testStepLogger.log("scope1", "Message 3")

        // then
        assertEquals(listOf("Message 1", "Message 2", "Message 3"), testStepLogger.getLogsAndClean("scope1"))
    }

    @Test
    fun `test getLogsAndClean returns copy of logs and clears original`() {
        // given
        testStepLogger.clear("scope1")
        testStepLogger.log("scope1", "Message 1")
        testStepLogger.log("scope1", "Message 2")

        // when
        val logs = testStepLogger.getLogsAndClean("scope1")

        // then
        assertEquals(listOf("Message 1", "Message 2"), logs)
        assertTrue(testStepLogger.getLogsAndClean("scope1").isEmpty())
    }

    @Test
    fun `test getLogsAndClean returns empty list when no logs`() {
        // given
        testStepLogger.clear("scope1")

        // then
        assertTrue(testStepLogger.getLogsAndClean("scope1").isEmpty())
    }

    @Test
    fun `test getLogsAndClean returns empty list for a scope which never logged`() {
        assertTrue(testStepLogger.getLogsAndClean("unknown").isEmpty())
    }

    @Test
    fun `test clear removes all log records of the scope`() {
        // given
        testStepLogger.clear("scope1")
        testStepLogger.log("scope1", "Message 1")
        testStepLogger.log("scope1", "Message 2")

        // when
        testStepLogger.clear("scope1")

        // then
        assertTrue(testStepLogger.getLogsAndClean("scope1").isEmpty())
    }

    @Test
    fun `test clear does nothing when log records already empty`() {
        // when
        testStepLogger.clear("scope1")
        testStepLogger.clear("scope1")

        // then
        assertTrue(testStepLogger.getLogsAndClean("scope1").isEmpty())
    }

    @Test
    fun `test that log records order is preserved`() {
        // given
        val expectedMessages = List(100) { i -> "Message $i" }
        testStepLogger.clear("scope1")

        // when
        expectedMessages.forEach { testStepLogger.log("scope1", it) }

        // then
        assertEquals(expectedMessages, testStepLogger.getLogsAndClean("scope1"))
    }

    @Test
    fun `test log records of a scope are not visible to other scopes`() {
        // given
        testStepLogger.clear("scope1")
        testStepLogger.clear("scope2")

        // when
        testStepLogger.log("scope1", "Message of scope 1")
        testStepLogger.log("scope2", "Message of scope 2")

        // then
        assertEquals(listOf("Message of scope 1"), testStepLogger.getLogsAndClean("scope1"))
        assertEquals(listOf("Message of scope 2"), testStepLogger.getLogsAndClean("scope2"))
    }

    @Test
    fun `test clear of a scope does not affect log records of other scopes`() {
        // given
        testStepLogger.clear("scope1")
        testStepLogger.clear("scope2")
        testStepLogger.log("scope1", "Message of scope 1")
        testStepLogger.log("scope2", "Message of scope 2")

        // when
        testStepLogger.clear("scope1")

        // then
        assertTrue(testStepLogger.getLogsAndClean("scope1").isEmpty())
        assertEquals(listOf("Message of scope 2"), testStepLogger.getLogsAndClean("scope2"))
    }

    @Test
    fun `test getLogsAndClean of a scope does not take log records of other scopes`() {
        // given
        testStepLogger.clear("scope1")
        testStepLogger.clear("scope2")
        testStepLogger.log("scope1", "Message of scope 1")
        testStepLogger.log("scope2", "Message of scope 2")

        // when
        testStepLogger.getLogsAndClean("scope1")

        // then
        assertEquals(listOf("Message of scope 2"), testStepLogger.getLogsAndClean("scope2"))
    }

    @Test
    fun `test clear without a scope removes log records of all scopes`() {
        // given
        testStepLogger.clear("scope1")
        testStepLogger.clear("scope2")
        testStepLogger.log("scope1", "Message of scope 1")
        testStepLogger.log("scope2", "Message of scope 2")

        // when
        testStepLogger.clear()

        // then
        assertTrue(testStepLogger.getLogsAndClean("scope1").isEmpty())
        assertTrue(testStepLogger.getLogsAndClean("scope2").isEmpty())
    }

    @Test
    fun `test log without a scope is dropped when no scope collects unscoped records`() {
        // given
        testStepLogger.clear("scope1")

        // when
        testStepLogger.log(null, "Message outside of any scope")

        // then
        assertTrue(testStepLogger.getLogsAndClean("scope1").isEmpty())
    }

    @Test
    fun `test log of a scope which has not started is dropped`() {
        // given
        testStepLogger.clear("scope1")

        // when
        testStepLogger.log("not started", "Message of a scope with no buffer")

        // then
        assertTrue(testStepLogger.getLogsAndClean("scope1").isEmpty())
        assertTrue(testStepLogger.getLogsAndClean("not started").isEmpty())
    }

    @Test
    fun `test log without a scope is collected by the scope for unscoped records`() {
        // given
        val testStepLogger = DefaultTestStepLogger("default")
        testStepLogger.clear("default")

        // when
        testStepLogger.log(null, "Message outside of any scope")

        // then
        assertEquals(listOf("Message outside of any scope"), testStepLogger.getLogsAndClean("default"))
    }

    @Test
    fun `test log of a scope which has not started is collected by the scope for unscoped records`() {
        // given
        val testStepLogger = DefaultTestStepLogger("default")

        // when
        testStepLogger.log("not started", "Message of a scope with no buffer")

        // then
        assertEquals(listOf("Message of a scope with no buffer"), testStepLogger.getLogsAndClean("default"))
    }

    @Test
    fun `test unscoped records do not end up in other scopes`() {
        // given
        val testStepLogger = DefaultTestStepLogger("default")
        testStepLogger.clear("default")
        testStepLogger.clear("scope1")

        // when
        testStepLogger.log(null, "Message outside of any scope")
        testStepLogger.log("scope1", "Message of scope 1")

        // then
        assertEquals(listOf("Message of scope 1"), testStepLogger.getLogsAndClean("scope1"))
        assertEquals(listOf("Message outside of any scope"), testStepLogger.getLogsAndClean("default"))
    }

    @Test
    fun `test taking the log records of a scope while it logs neither loses nor repeats a record`() {
        // given
        val recordsCount = 20000
        testStepLogger.clear("scope1")
        val executor = Executors.newSingleThreadExecutor()
        val taken = mutableListOf<String>()

        try {
            // when
            val writer = executor.submit { repeat(recordsCount) { testStepLogger.log("scope1", "record $it") } }
            while (!writer.isDone) {
                taken.addAll(testStepLogger.getLogsAndClean("scope1"))
            }
            writer.get(30, TimeUnit.SECONDS)
            taken.addAll(testStepLogger.getLogsAndClean("scope1"))
        } finally {
            executor.shutdownNow()
        }

        // then
        assertEquals(List(recordsCount) { "record $it" }, taken)
    }

    @Test
    fun `test scopes logging at the same time collect only their own log records`() {
        // given
        val scopeIds = List(8) { "scope$it" }
        val recordsPerScope = 2000
        scopeIds.forEach { testStepLogger.clear(it) }
        val startSignal = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(scopeIds.size)

        try {
            // when
            val futures = scopeIds.map { scopeId ->
                executor.submit {
                    startSignal.await()
                    repeat(recordsPerScope) { testStepLogger.log(scopeId, "$scopeId record $it") }
                }
            }
            startSignal.countDown()
            futures.forEach { it.get(30, TimeUnit.SECONDS) }
        } finally {
            executor.shutdownNow()
        }

        // then
        scopeIds.forEach { scopeId ->
            assertEquals(
                List(recordsPerScope) { "$scopeId record $it" },
                testStepLogger.getLogsAndClean(scopeId)
            )
        }
    }
}
