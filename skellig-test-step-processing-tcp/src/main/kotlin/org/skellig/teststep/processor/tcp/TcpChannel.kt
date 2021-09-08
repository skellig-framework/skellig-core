package org.skellig.teststep.processor.tcp

import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.tcp.model.TcpDetails
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TcpChannel(private val tcpDetails: TcpDetails) : Closeable {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(TcpChannel::class.java)

        private const val DEFAULT_TIMEOUT = 30000
    }

    private var socket: Socket? = null
    private var inputStream: DataInputStream? = null
    private var outputStream: DataOutputStream? = null
    private var consumerThread: ExecutorService? = null

    private fun lazyConnectSocket() {
        if (socket == null) {
            try {
                socket = Socket()
                socket!!.keepAlive = tcpDetails.isKeepAlive
                socket!!.soTimeout = DEFAULT_TIMEOUT
                socket!!.tcpNoDelay = tcpDetails.isKeepAlive
                socket!!.connect(InetSocketAddress(InetAddress.getByName(tcpDetails.hostName), tcpDetails.port))
                inputStream = DataInputStream(socket!!.getInputStream())
                outputStream = DataOutputStream(socket!!.getOutputStream())

                LOGGER.info("TCP channel has been connected on ${getRemoteAddressAsString()}")
            } catch (e: IOException) {
                throw TestStepProcessingException(e.message, e)
            }
        }
    }

    fun send(request: Any?) {
        val messageAsBytes = when (request) {
            is ByteArray -> request
            is String -> request.toByteArray()
            else -> null
        }
        messageAsBytes?.let {
            lazyConnectSocket()
            try {
                outputStream?.let {
                    it.write(messageAsBytes, 0, messageAsBytes.size)
                    it.flush()
                }
            } catch (e: Exception) {
                LOGGER.error("Failed to send request to ${getRemoteAddressAsString()}", e)
            }
        } ?: error("Request was not sent to ${getRemoteAddressAsString()} as it must be String or Byte Array");
    }

    fun read(timeout: Int, bufferSize: Int): Any? {
        lazyConnectSocket()
        return try {
            initTimeout(timeout)
            readAllBytes(bufferSize)
        } catch (e: Exception) {
            LOGGER.error("Failed to read response from ${getRemoteAddressAsString()}", e)
            null
        }
    }

    fun consume(response: Any?, timeout: Int, bufferSize: Int, responseHandler: (message: Any?) -> Unit) {
//        if (consumerThread != null) {
//            close()
//        }
        if (consumerThread == null || consumerThread!!.isShutdown) {
            consumerThread = Executors.newCachedThreadPool()
        }
        lazyConnectSocket()
        consumerThread?.execute {
            initTimeout(timeout)
            while (!consumerThread!!.isShutdown) {
                val bytes = readAllBytes(bufferSize)
                responseHandler(bytes)
                response?.let { send(it) }
            }
        }
    }

    private fun closeConsumer() {
        consumerThread?.shutdownNow()
    }

    @Synchronized
    override fun close() {
        try {
            closeConsumer()
            if (socket?.isClosed == false) {
                LOGGER.debug("Closing TCP channel ${getRemoteAddressAsString()}")
                inputStream!!.close()
                outputStream!!.close()
                socket?.close()
            }
        } catch (e: Exception) {
            LOGGER.warn("Could not safely close TCP channel ${getRemoteAddressAsString()}", e)
        }
    }

    private fun initTimeout(timeout: Int) {
        if (timeout > 0) {
            socket?.soTimeout = timeout
        } else {
            socket?.soTimeout = DEFAULT_TIMEOUT
        }
    }

    @Throws(IOException::class)
    private fun readAllBytes(bufferSize: Int): ByteArray? {
        var read: Int
        var response = ByteArray(0)
        val bytes = ByteArray(bufferSize)
        while (inputStream!!.read(bytes).also { read = it } != -1) {
            val shift = response.size
            response = response.copyOf(read + response.size)
            var i = shift
            var j = 0
            while (j < read) {
                response[i++] = bytes[j++]
            }
            // break the cycle if nothing remains in the stream
            if(inputStream!!.available() <= 0){
                break
            }
        }

        return if (response.isEmpty()) null
        else {
            LOGGER.debug("Tcp channel ${getRemoteAddressAsString()} received ${response.size}")
            response
        }
    }

    private fun getRemoteAddressAsString() = socket?.let { "'${socket!!.inetAddress}:${socket!!.port}'" } ?: ""

}