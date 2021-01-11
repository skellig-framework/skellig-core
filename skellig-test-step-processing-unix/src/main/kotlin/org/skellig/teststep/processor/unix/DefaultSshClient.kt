package org.skellig.teststep.processor.unix

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.IOUtils
import net.schmizz.sshj.connection.channel.direct.Session
import org.skellig.teststep.processing.exception.TestStepProcessingException
import java.io.IOException
import java.util.concurrent.TimeUnit

class DefaultSshClient private constructor(private val host: String,
                                           private val port: Int,
                                           private val user: String?,
                                           private val password: String?,
                                           private val privateSshKeyPath: String?) : SSHClient() {

    companion object {
        private const val DEFAULT_CONNECT_TIMEOUT = 10000
    }

    init {
        connectTimeout = DEFAULT_CONNECT_TIMEOUT
    }

    private var sshSession: Session? = null

    fun runShellCommand(command: String, timeoutSec: Int): String {
        startSshSessionLazy()
        var response = ""
        try {
            val cmd = sshSession!!.exec(command)
            cmd.join(timeoutSec.toLong(), TimeUnit.SECONDS)

            IOUtils.readFully(cmd.inputStream).use { outputStream -> response = outputStream.toString() }
        } catch (ex: Exception) {
            //log later
        }
        return response
    }

    @Synchronized
    override fun close() {
        try {
            sshSession?.let {
                sshSession!!.close()
                sshSession = null
            }
            super.close()
        } catch (ex: Exception) {
            //log later
        }
    }

    @Synchronized
    private fun startSshSessionLazy() {
        if (sshSession == null || !sshSession!!.isOpen) {
            try {
                if (!(isConnected && isAuthenticated)) {
                    createAndConnectSshClient()
                }
                sshSession = super.startSession()
                sshSession?.allocateDefaultPTY()
            } catch (ex: Exception) {
                throw TestStepProcessingException(ex.message, ex)
            }
        }
    }

    @Throws(IOException::class)
    private fun createAndConnectSshClient() {
        addHostKeyVerifier { _, _, _ -> true }
        connect(host, port)

        privateSshKeyPath?.let {
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