package org.skellig.runner.junit.report.attachment.log.fromfile

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.IOUtils
import net.schmizz.sshj.connection.channel.direct.Session
import java.io.IOException
import java.util.concurrent.TimeUnit

class ExtractLogRecordsUnixCommandRunner(private val logExtractionDetails: LogExtractionDetails): SSHClient() {

    companion object {
        private const val DEFAULT_CONNECT_TIMEOUT = 30000L
    }

    fun getLogRecords(): String {
        var response = ""
        startSshSession().use { sshSession ->
            try {
                sshSession.allocateDefaultPTY()
                val cmd = sshSession.exec("tail -n${logExtractionDetails.maxRecords} ${logExtractionDetails.path}")
                cmd.join(DEFAULT_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)

                IOUtils.readFully(cmd.inputStream).use { outputStream -> response = outputStream.toString() }
            } catch (ex: Exception) {
                //log later
                response = "Failed to get data from log file '${logExtractionDetails.path}': ${ex.message ?: ""}"
            }
        }

        return response
    }

    private fun startSshSession(): Session {
        if (!(isConnected && isAuthenticated)) {
            createAndConnectSshClient()
        }
        return super.startSession()
    }

    @Throws(IOException::class)
    private fun createAndConnectSshClient() {
        addHostKeyVerifier { _, _, _ -> true }
        connect(logExtractionDetails.host, logExtractionDetails.port)

        logExtractionDetails.sshKeyPath?.let {
            authPublickey(logExtractionDetails.userName, logExtractionDetails.sshKeyPath)
        } ?: this.authPassword(logExtractionDetails.userName, logExtractionDetails.password)
    }
}