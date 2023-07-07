package org.skellig.runner.junit.report.attachment.log.fromfile

internal class ExtractLocalLogRecordsUnixCommandRunner(private val logExtractionDetails: LogExtractionDetails) : ExtractLogRecordsUnixCommandRunner {

    override fun getLogRecords(): String {
        return Runtime.getRuntime()
            .exec("tail -n${logExtractionDetails.maxRecords} ${logExtractionDetails.path}")
            .inputStream
            .bufferedReader()
            .use { it.readText() }
    }

}