package org.skellig.teststep.processor.tcp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.tcp.model.TcpDetails
import java.io.Closeable
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

/**
 * Represents a TCP channel that allows sending data over a TCP connection and receiving responses.
 * The channel can also consume incoming messages and respond to them asynchronously.
 *
 * The TCP Socket timeout is set to 30 sec for read operations, unless the other timeout > 0 is provided in methods
 * [TcpChannel.read] or [TcpChannel.consume].
 *
 * @property tcpDetails The details of [TcpDetails].
 */
class TcpChannel(private val tcpDetails: TcpDetails) : Closeable {

    companion object {
        private const val DEFAULT_TIMEOUT = 30000
    }

    private val log = logger<TcpChannel>()
    private var socket: Socket? = null
    private var inputStream: DataInputStream? = null
    private var outputStream: DataOutputStream? = null
    private var consumerThread: ExecutorService? = null

    /**
     * Sends a request to the TCP channel.
     * The request can be either a byte array or a string.
     * If the request is a byte array, it is sent as is.
     * If the request is a string, it is converted to a byte array using the default character encoding.
     *
     * @param request the request to send, can be a byte array or a string
     *
     * @throws IllegalArgumentException if the request is not a byte array or a string
     */
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
                log.error("Failed to send a message to TCP address '${getRemoteAddressAsString()}'", e)
            }
        } ?: error("Request was not sent to '${getRemoteAddressAsString()}' as it must be String or Byte Array")
    }

    /**
     * Reads data from the TCP channel with the specified timeout and buffer size.
     *
     * @param timeout the timeout in milliseconds for reading data from the channel. If 0 then default timeout 30 sec is applied.
     * @param bufferSize the size of the buffer used for reading data
     * @return the response received from the channel, or null if an exception occurred during the read operation
     */
    fun read(timeout: Int, bufferSize: Int): Any? {
        lazyConnectSocket()
        return try {
            initTimeout(timeout)
            readAllBytes(bufferSize)
        } catch (e: Exception) {
            log.error("Failed to read response from '${getRemoteAddressAsString()}'", e)
            null
        }
    }

    /**
     * Consumes data from a TCP channel.
     *
     * @param timeout the timeout in milliseconds for reading data from the channel. If 0 then default timeout 30 sec is applied.
     * @param bufferSize the size of the buffer used for reading data
     * @param responseHandler the handler function to process the received response
     */
    fun consume(timeout: Int, bufferSize: Int, responseHandler: (message: Any?) -> Unit) {
        if (consumerThread == null || consumerThread!!.isShutdown) {
            consumerThread = Executors.newCachedThreadPool()
        }
        lazyConnectSocket()
        log.info("Start listener for TCP channel: $tcpDetails")
        consumerThread?.execute {
            initTimeout(timeout)
            val consumerCallbackJob = ConsumerCallbackJob(responseHandler)
            try {
                while (!consumerThread!!.isShutdown) {
                    val data = readAllBytes(bufferSize)
                    consumerCallbackJob.execute(data)
                }
            } finally {
                log.debug { "Stopped message consumer of TCP address '${getRemoteAddressAsString()}'" }
            }
        }
    }

    /**
     * Closes the TCP channel, including the input and output streams and the socket connection.
     * If an exception occurs during the close operation, it's ignored but logged as a warning message with the reason.
     */
    @Synchronized
    override fun close() {
        try {
            closeConsumer()
            if (socket?.isClosed == false) {
                log.debug { "Closing TCP channel '$tcpDetails' with address '${getRemoteAddressAsString()}'" }
                inputStream!!.close()
                outputStream!!.close()
                socket?.close()
            }
        } catch (e: Exception) {
            log.warn("Could not safely close TCP channel '$tcpDetails'. Reason: ${e.message}")
        }
    }

    private fun closeConsumer() {
        consumerThread?.shutdownNow()
    }

    private fun lazyConnectSocket() {
        if (socket == null) {
            log.debug { "Start to connect to TCP channel '$tcpDetails'" }
            try {
                socket = Socket()
                socket!!.keepAlive = tcpDetails.isKeepAlive
                socket!!.soTimeout = DEFAULT_TIMEOUT
                socket!!.tcpNoDelay = tcpDetails.isKeepAlive
                socket!!.connect(InetSocketAddress(InetAddress.getByName(tcpDetails.hostName), tcpDetails.port))
                inputStream = DataInputStream(socket!!.getInputStream())
                outputStream = DataOutputStream(socket!!.getOutputStream())

                log.debug { "Successfully connected to TCP channel '$tcpDetails'" }
            } catch (e: IOException) {
                throw TestStepProcessingException(e.message, e)
            }
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
            log.debug { "Received ${response.size} bytes from address '${getRemoteAddressAsString()}'" }
            response
        }
    }

    private fun getRemoteAddressAsString() = socket?.let { "'${socket!!.inetAddress}:${socket!!.port}'" } ?: ""

    private class ConsumerCallbackJob(val callback: (message: Any?) -> Unit) : CoroutineScope {
        private var job: Job = Job()

        override val coroutineContext: CoroutineContext
            get() = Dispatchers.IO + job

        fun execute(body: Any?) = launch {
            callback(body)
        }
    }
}