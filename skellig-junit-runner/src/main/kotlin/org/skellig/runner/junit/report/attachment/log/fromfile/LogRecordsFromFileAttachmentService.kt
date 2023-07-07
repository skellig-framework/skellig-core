package org.skellig.runner.junit.report.attachment.log.fromfile

import org.skellig.runner.junit.report.attachment.AttachmentService

class LogRecordsFromFileAttachmentService(private val logExtractionDetails: LogExtractionDetails) : AttachmentService<LogRecordsFromFileAttachment> {

    private var extractLogRecordsUnixCommandRunner =
        if (logExtractionDetails.host.lowercase() == "localhost" || logExtractionDetails.host == "127.0.0.1")
            ExtractLocalLogRecordsUnixCommandRunner(logExtractionDetails)
        else ExtractRemoteLogRecordsUnixCommandRunner(logExtractionDetails)

    override fun getData(): LogRecordsFromFileAttachment {
        return LogRecordsFromFileAttachment("${logExtractionDetails.host}: ${logExtractionDetails.path}", extractLogRecordsUnixCommandRunner.getLogRecords())
    }

    override fun isApplicable(isTestPass: Boolean): Boolean = !isTestPass

}