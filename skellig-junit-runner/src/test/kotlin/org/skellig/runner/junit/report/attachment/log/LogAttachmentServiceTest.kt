package org.skellig.runner.junit.report.attachment.log

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LogAttachmentServiceTest {

    @Test
    fun testGetData() {
        val testStepLogger = mock<TestStepLogger>()
        val logs = mock<List<String>>()
        whenever(testStepLogger.getLogsAndClean()).thenReturn(logs)

        assertEquals(logs, LogAttachmentService(testStepLogger).getData().data)
    }
}