package org.skellig.teststep.processor.unix.model

class UnixShellHostDetails private constructor(val hostName: String,
                                               val hostAddress: String,
                                               val port: Int,
                                               val sshKeyPath: String?,
                                               val userName: String?,
                                               val password: String?) {

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
            return UnixShellHostDetails(hostName ?: error("Host name must not be null. Please set any unique name"),
                    hostAddress ?: error("Host address must not be null"), port, sshKeyPath, userName, password)
        }
    }
}