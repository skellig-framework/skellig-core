package org.skellig.runner.junit.report.attachment.log.fromfile

class LogExtractionDetails private constructor(
    val host: String,
    val port: Int,
    val sshKeyPath: String?,
    val userName: String?,
    val password: String?,
    val path: String,
    val maxRecords: Int
) {

    class Builder {

        private var host: String = ""
        private var port = 0
        private var sshKeyPath: String? = null
        private var userName: String? = null
        private var password: String? = null
        private var path: String = ""
        private var maxRecords: Int = 100

        fun withHost(host: String) = apply {
            this.host = host
        }

        fun withPort(port: Int) = apply {
            this.port = port
        }

        fun withSshKeyPath(sshKeyPath: String?) = apply {
            this.sshKeyPath = sshKeyPath
        }

        fun withUserName(userName: String?) = apply {
            this.userName = userName
        }

        fun withPassword(password: String?) = apply {
            this.password = password
        }

        fun withPath(path: String) = apply {
            this.path = path
        }

        fun withMaxRecords(maxRecords: Int?) = apply {
            maxRecords?.let { this.maxRecords = it }
        }

        fun build(): LogExtractionDetails {
            return LogExtractionDetails(host, port, sshKeyPath, userName, password, path, maxRecords)
        }
    }
}