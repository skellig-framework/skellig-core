package org.skellig.runner.junit.report.attachment.log

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DefaultTestStepLoggerTest {

    @Test
    fun testLog() {
        val logger = DefaultTestStepLogger()
        logger.log("r1")

        assertEquals(listOf("r1"), logger.getLogsAndClean())

        logger.log("r2")

        assertEquals(listOf("r2"), logger.getLogsAndClean())
    }
}