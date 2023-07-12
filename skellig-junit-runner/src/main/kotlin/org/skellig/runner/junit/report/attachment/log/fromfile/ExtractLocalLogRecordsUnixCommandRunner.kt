package org.skellig.runner.junit.report.attachment.log.fromfile

internal open class ExtractLocalLogRecordsUnixCommandRunner(private val logExtractionDetails: LogExtractionDetails) : ExtractLogRecordsUnixCommandRunner {

    override fun getLogRecords(): String {
        return try {
            val runtime = createRuntime()
            runtime
                .exec("tail -n${logExtractionDetails.maxRecords} ${logExtractionDetails.path}")
                .inputStream
                .bufferedReader()
                .use { it.readText() }
        } catch (ex: Exception) {
            "Failed to extract log records from file '${logExtractionDetails.path}': ${ex.message ?: ex.cause?.message ?: "no error information"}"
        }
    }

    internal open fun createRuntime(): Runtime {
        return  Runtime.getRuntime()
    }

}