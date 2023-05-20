package org.skellig.teststep.processor.unix

import com.typesafe.config.Config
import org.skellig.teststep.processor.unix.model.UnixShellHostDetails
import java.util.*

class UnixShellConfigReader {

    companion object {
        private const val UNIX_SHELL_CONFIG_KEYWORD = "unix-shell.hosts"
    }

    fun read(config: Config): Collection<UnixShellHostDetails> {
        Objects.requireNonNull(config, "Unix Shell config cannot be null")

        var unixShellDetails = emptyList<UnixShellHostDetails>()
        if (config.hasPath(UNIX_SHELL_CONFIG_KEYWORD)) {
            val anyRefList = config.getAnyRefList(UNIX_SHELL_CONFIG_KEYWORD) as List<Map<*, *>>
            unixShellDetails = anyRefList
                    .map { createUnixShellDetails(it) }
                    .toList()
        }
        return unixShellDetails
    }

    private fun createUnixShellDetails(rawJdbcDetails: Map<*, *>): UnixShellHostDetails {
        val hostName = rawJdbcDetails["hostName"] as String? ?: error("Server name must be declared for JDBC instance")
        val hostAddress = rawJdbcDetails["hostAddress"] as String? ?: error("Driver class name must be declared for JDBC instance")
        val port = rawJdbcDetails["port"] as Int? ?: 22
        val userName = rawJdbcDetails["userName"] as String?
        val password = rawJdbcDetails["password"] as String?
        val sshKeyPath = rawJdbcDetails["sshKeyPath"] as String?

        return UnixShellHostDetails.Builder()
                .withHostName(hostName)
                .withHostAddress(hostAddress)
                .withPort(port)
                .withUserName(userName)
                .withPassword(password)
                .withSshKeyPath(sshKeyPath)
                .build()
    }
}