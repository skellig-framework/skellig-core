package org.skellig.runner.junit.report.attachment.log.fromfile

import com.typesafe.config.Config
import java.util.*

class LogExtractorConfigReader {

    companion object {
        private const val LOGS_CONFIG_KEYWORD = "report.attachments.recordsFromLogFile.paths"
    }

    fun read(config: Config): Collection<LogExtractionDetails> {
        Objects.requireNonNull(config, "Unix Shell config cannot be null")

        var logExtractionDetails = emptyList<LogExtractionDetails>()
        if (config.hasPath(LOGS_CONFIG_KEYWORD)) {
            val anyRefList = config.getAnyRefList(LOGS_CONFIG_KEYWORD) as List<*>
            logExtractionDetails = anyRefList
                .filterIsInstance<Map<*, *>>()
                .map { createLogExtractionDetails(it) }
                .toList()
        }
        return logExtractionDetails
    }

    private fun createLogExtractionDetails(details: Map<*, *>): LogExtractionDetails {
        val host = details["host"] as String? ?: error("Server name must be declared Log Extraction details")
        val port = details["port"] as Int? ?: 22
        val userName = details["userName"] as String?
        val password = details["password"] as String?
        val sshKeyPath = details["sshKeyPath"] as String?
        val path = details["path"] as String? ?: error("Path to log file must be set in the $LOGS_CONFIG_KEYWORD")
        val maxRecords = details["maxRecords"] as Int?

        return LogExtractionDetails.Builder()
            .withHost(host)
            .withPort(port)
            .withUserName(userName)
            .withPassword(password)
            .withSshKeyPath(sshKeyPath)
            .withPath(path)
            .withMaxRecords(maxRecords)
            .build()
    }
}