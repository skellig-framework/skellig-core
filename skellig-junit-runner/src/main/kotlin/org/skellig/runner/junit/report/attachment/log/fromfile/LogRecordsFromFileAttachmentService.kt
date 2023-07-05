package org.skellig.runner.junit.report.attachment.log.fromfile

import org.skellig.runner.junit.report.attachment.AttachmentService

class LogRecordsFromFileAttachmentService(val logExtractionDetails: LogExtractionDetails) : AttachmentService<LogRecordsFromFileAttachment> {

    private var extractLogRecordsUnixCommandRunner: ExtractLogRecordsUnixCommandRunner = ExtractLogRecordsUnixCommandRunner(logExtractionDetails)

    override fun getData(): LogRecordsFromFileAttachment {
        return LogRecordsFromFileAttachment(logExtractionDetails.path, extractLogRecordsUnixCommandRunner.getLogRecords())
    }

}