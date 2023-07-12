package org.skellig.runner.junit.report.attachment.log.fromfile

internal class ExtractLocalLogRecordsUnixCommandRunner(private val logExtractionDetails: LogExtractionDetails) : ExtractLogRecordsUnixCommandRunner {

    override fun getLogRecords(): String {
        return try {
            Runtime.getRuntime()
                .exec("tail -n${logExtractionDetails.maxRecords} ${logExtractionDetails.path}")
                .inputStream
                .bufferedReader()
                .use { it.readText() }
        } catch (ex: Exception) {
            "Failed to extract log records from file '${logExtractionDetails.path}': ${ex.message ?: ex.cause?.message ?: "no error information"}"
        }
    }

}