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

    companion object {
        private const val DEFAULT_TIMEOUT = 30000
    }

    private var socket: Socket
    private var inputStream: DataInputStream? = null
    private var outputStream: DataOutputStream? = null

    init {
        try {
            socket = Socket()
            socket.keepAlive = tcpDetails.isKeepAlive
            socket.soTimeout = DEFAULT_TIMEOUT
            socket.connect(InetSocketAddress(InetAddress.getByName(tcpDetails.hostName), tcpDetails.port))
            inputStream = DataInputStream(socket.getInputStream())
            outputStream = DataOutputStream(socket.getOutputStream())
        } catch (e: IOException) {
            throw TestStepProcessingException(e.message, e)
        }
    }

    fun send(request: Any?) {
        val messageAsBytes = when (request) {
            is ByteArray -> request
            is String -> request.toByteArray()
            else -> null
        }
        messageAsBytes?.let {
            try {
                outputStream?.let {
                    it.write(messageAsBytes, 0, messageAsBytes.size)
                    it.flush()
                }
            } catch (e: Exception) {
                // log later
                e.printStackTrace()
            }
        } ?: error("Request was not sent to ${socket.remoteSocketAddress} as it must be String or Byte Array");
    }

    fun read(timeout: Int, bufferSize: Int): Any? {
        try {
            if (timeout > 0) {
                socket.soTimeout = timeout
            } else {
                socket.soTimeout = DEFAULT_TIMEOUT
            }
            return readAllBytes(bufferSize)
        } catch (e: Exception) {
            //log later
            e.printStackTrace();
        }
        return null
    }

    @Synchronized
    override fun close() {
        try {
            if (socket.isConnected) {
                if (!socket.isClosed) {
                    inputStream!!.close()
                    outputStream!!.close()
                }
                socket.close()
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
}