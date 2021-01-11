package org.skellig.teststep.processor.tcp

import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.tcp.model.TcpDetails
import java.io.Closeable
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class TcpChannel(tcpDetails: TcpDetails) : Closeable {

    private var socket: Socket? = null
    private var inputStream: DataInputStream? = null
    private var outputStream: DataOutputStream? = null

    fun send(request: Any?) {
        request?.let {
            try {
                val messageAsBytes = request as ByteArray
                outputStream?.let {
                    it.write(messageAsBytes, 0, messageAsBytes.size)
                    it.flush()
                }
            } catch (e: Exception) {
                // log later
            }
        }
    }

    fun read(timeout: Int, bufferSize: Int): Any? {
        try {
            return socket?.let {
                if (timeout > 0) {
                    it.soTimeout = timeout
                } else {
                    it.soTimeout = DEFAULT_TIMEOUT
                }
                readAllBytes(bufferSize)
            }
        } catch (e: Exception) {
            //log later
        }
        return null
    }

    @Synchronized
    override fun close() {
        try {
            socket?.let {
                if (it.isConnected) {
                    if (!it.isClosed) {
                        inputStream!!.close()
                        outputStream!!.close()
                    }
                    it.close()
                }
            }
        } catch (e: Exception) {
            //log later
        }
    }

    @Throws(IOException::class)
    private fun readAllBytes(bufferSize: Int): ByteArray? {
        var read: Int
        var response = ByteArray(0)
        val bytes = ByteArray(bufferSize)
        if (inputStream!!.read(bytes).also { read = it } != -1) {
            val shift = response.size
            response = response.copyOf(read + response.size)
            var i = shift
            var j = 0
            while (j < read) {
                response[i++] = bytes[j++]
            }
        }
        return if (response.isEmpty()) null else response
    }

    companion object {
        private const val DEFAULT_TIMEOUT = 30000
    }

    init {
        try {
            socket = Socket()
            socket!!.keepAlive = tcpDetails.isKeepAlive
            socket!!.soTimeout = DEFAULT_TIMEOUT
            socket!!.connect(InetSocketAddress(InetAddress.getByName(tcpDetails.hostName), tcpDetails.port))
            inputStream = DataInputStream(socket!!.getInputStream())
            outputStream = DataOutputStream(socket!!.getOutputStream())
        } catch (e: IOException) {
            throw TestStepProcessingException(e.message, e)
        }
    }
}