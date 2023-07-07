package org.skellig.runner.junit.report.attachment.log

import org.skellig.runner.junit.report.attachment.AttachmentService

internal class LogAttachmentService(private val testStepLogger: TestStepLogger) : AttachmentService<LogAttachment> {
    override fun getData(): LogAttachment {
        return LogAttachment(testStepLogger.getLogsAndClean())
    }
}