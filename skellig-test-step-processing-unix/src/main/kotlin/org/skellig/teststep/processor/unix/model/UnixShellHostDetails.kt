package org.skellig.teststep.processor.unix.model

/**
 * Class representing the details of a Unix shell host.
 * You must provide either a password or path to ssh key in order to connect to [hostAddress].
 *
 * @param hostName the name of the host (used in test step file as a reference to the config).
 * It may be any name (not necessary the same as [hostAddress]) as long as it's unique in the Skellig Config file.
 * @param hostAddress the IP address or hostname of the host
 * @param port the port number for SSH connection (default is 22)
 * @param sshKeyPath the file path of the SSH key (optional). It's used if [password] is not provided.
 * @param userName the username for SSH connection (optional)
 * @param password the password for SSH connection (optional)
 */
class UnixShellHostDetails private constructor(
    val hostName: String,
    val hostAddress: String,
    val port: Int,
    val sshKeyPath: String?,
    val userName: String?,
    val password: String?
) {

    class Builder {

        private var hostName: String? = null
        private var hostAddress: String? = null
        private var port = 0
        private var sshKeyPath: String? = null
        private var userName: String? = null
        private var password: String? = null

        fun withHostName(name: String?) = apply {
            hostName = name
        }

        fun withHostAddress(host: String?) = apply {
            hostAddress = host
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

        fun build(): UnixShellHostDetails {
            return UnixShellHostDetails(
                hostName ?: error("Host name must not be null. Please set any unique name"),
                hostAddress ?: error("Host address must not be null"), port, sshKeyPath, userName, password
            )
        }
    }
}