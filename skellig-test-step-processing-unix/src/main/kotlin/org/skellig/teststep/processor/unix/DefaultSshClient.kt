package org.skellig.teststep.processor.unix

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.IOUtils
import net.schmizz.sshj.connection.channel.direct.Session
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.util.logger
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * This class represents an SSH client that can be used to run shell commands on remote hosts.
 *
 * @property host The host address of the SSH server.
 * @property port The port number of the SSH server.
 * @property user The username used for authentication. Can be null for password-less authentication with SSH keys.
 * @property password The password used for password-based authentication. Ignored if SSH keys are used for authentication.
 * @property privateSshKeyPath The file path of the SSH private key, used for key-based authentication. Ignored if password-based authentication is used.
 */
class DefaultSshClient private constructor(private val host: String,
                                           private val port: Int,
                                           private val user: String?,
                                           private val password: String?,
                                           private val privateSshKeyPath: String?) : SSHClient() {

    private val logger = logger<DefaultSshClient>()

    init {
        connectTimeout = 10000
    }

    private var sshSession: Session? = null

    /**
     * Executes a shell command on the remote host using SSH.
     *
     * @param command The shell command to execute.
     * @param timeout The maximum time in milliseconds to wait for the command to complete.
     * @return The response from the shell command. Returns null if error occurred, but logs it before.
     */
    fun runShellCommand(command: String, timeout: Int): String {
        startSshSessionLazy()
        var response = ""
        try {
            val cmd = sshSession!!.exec(command)
            cmd.join(timeout.toLong(), TimeUnit.MILLISECONDS)

            IOUtils.readFully(cmd.inputStream).use { outputStream -> response = outputStream.toString() }
        } catch (ex: Exception) {
            logger.error("Failed to run the shell command '$command' in '$host:$port'", ex)
        }
        return response
    }

    /**
     * Closes the SSH session.
     *
     * This method closes the SSH session if it is open. It also calls the close() method of the superclass.
     * If an exception occurs while closing the SSH session, it is logged with the logger.
     * This method is synchronized to ensure thread safety.
     */
    @Synchronized
    override fun close() {
        try {
            sshSession?.let {
                sshSession!!.close()
                sshSession = null
            }
            super.close()
        } catch (ex: Exception) {
            logger.error("Failed to close SSH Session of '$host:$port'", ex)
        }
    }

    @Synchronized
    private fun startSshSessionLazy() {
        if (sshSession == null || !sshSession!!.isOpen) {
            try {
                if (!(isConnected && isAuthenticated)) {
                    createAndConnectSshClient()
                }
                logger.info("Start new SSH session for '$host:$port'")
                sshSession = super.startSession()
                sshSession?.allocateDefaultPTY()
            } catch (ex: Exception) {
                throw TestStepProcessingException(ex.message, ex)
            }
        }
    }

    @Throws(IOException::class)
    private fun createAndConnectSshClient() {
        logger.info("Create new SSH connection to '$host:$port' with username '$user' and password '$password'")
        addHostKeyVerifier("")
        connect(host, port)

        privateSshKeyPath?.let {
            logger.info("Authenticate SSH connection to '$host:$port' with private key '$privateSshKeyPath'")
            authPublickey(user, privateSshKeyPath)
        } ?: this.authPassword(user, password)
    }

    internal class Builder {

        private var host: String? = null
        private var port = 0
        private var user: String? = null
        private var password: String? = null
        private var privateSshKeyPath: String? = null

        fun withHost(host: String?) = apply {
            this.host = host
        }

        fun withPort(port: Int) = apply {
            this.port = port
        }

        fun withUser(user: String?) = apply {
            this.user = user
        }

        fun withPassword(password: String?) = apply {
            this.password = password
        }

        fun withPrivateSshKeyPath(privateSshKeyPath: String?) = apply {
            this.privateSshKeyPath = privateSshKeyPath
        }

        fun build(): DefaultSshClient {
            return DefaultSshClient(host!!, port, user, password, privateSshKeyPath)
        }
    }
}