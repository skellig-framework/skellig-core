package org.skellig.runner.junit.report.attachment.log.fromfile

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.IOUtils
import net.schmizz.sshj.connection.channel.direct.Session
import java.util.concurrent.TimeUnit

internal open class ExtractRemoteLogRecordsUnixCommandRunner(private val logExtractionDetails: LogExtractionDetails): SSHClient(), ExtractLogRecordsUnixCommandRunner {

    companion object {
        const val DEFAULT_CONNECT_TIMEOUT = 30000L
    }

    override fun getLogRecords(): String {
        var response = ""
        startSshSession().use { sshSession ->
            try {
                sshSession.allocateDefaultPTY()
                val cmd = sshSession.exec("tail -n${logExtractionDetails.maxRecords} ${logExtractionDetails.path}")
                cmd.join(DEFAULT_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)

                IOUtils.readFully(cmd.inputStream).use { stream -> response = stream.toString() }
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
        return startSession()
    }

    private fun createAndConnectSshClient() {
        addHostKeyVerifier { _, _, _ -> true }
        connect(logExtractionDetails.host, logExtractionDetails.port)

        logExtractionDetails.sshKeyPath?.let {
            authPublickey(logExtractionDetails.userName, logExtractionDetails.sshKeyPath)
        } ?: this.authPassword(logExtractionDetails.userName, logExtractionDetails.password)
    }
}