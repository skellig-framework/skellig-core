package org.skellig.teststep.processor.unix

import com.typesafe.config.Config
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.unix.model.UnixShellHostDetails
import java.util.*


/**
 * Class that reads Unix Shell configuration from a Skellig [Config] and returns a collection of [UnixShellHostDetails].
 * It returns empty list if the configuration for 'unix-shell.hosts' property is not found in the Skellig [Config] file.
 */
class UnixShellConfigReader {

    companion object {
        private const val UNIX_SHELL_CONFIG_KEYWORD = "unix-shell.hosts"
    }

    private val log = logger<UnixShellConfigReader>()

    fun read(config: Config): Collection<UnixShellHostDetails> {
        Objects.requireNonNull(config, "Unix Shell config cannot be null")

        var unixShellDetails = emptyList<UnixShellHostDetails>()
        if (config.hasPath(UNIX_SHELL_CONFIG_KEYWORD)) {
            log.info("UNIX Shell configuration found in the Config file. Start to register its hosts")
            val anyRefList = config.getAnyRefList(UNIX_SHELL_CONFIG_KEYWORD) as List<*>
            unixShellDetails = anyRefList
                .mapNotNull { createUnixShellDetails(it) }
                .toList()
        }
        return unixShellDetails
    }

    private fun createUnixShellDetails(rawJdbcDetails: Any?): UnixShellHostDetails? {
        return (rawJdbcDetails as Map<*, *>?)?.let {
            val hostName = it["hostName"] as String? ?: error("Server name must be declared for JDBC instance")
            val hostAddress = it["hostAddress"] as String? ?: error("Driver class name must be declared for JDBC instance")
            val port = it["port"] as Int? ?: 22
            val userName = it["userName"] as String?
            val password = it["password"] as String?
            val sshKeyPath = it["sshKeyPath"] as String?

            UnixShellHostDetails.Builder()
                .withHostName(hostName)
                .withHostAddress(hostAddress)
                .withPort(port)
                .withUserName(userName)
                .withPassword(password)
                .withSshKeyPath(sshKeyPath)
                .build()
        }
    }
}