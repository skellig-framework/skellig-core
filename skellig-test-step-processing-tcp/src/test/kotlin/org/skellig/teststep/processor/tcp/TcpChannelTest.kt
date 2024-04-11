package org.skellig.teststep.processor.tcp

import org.apache.commons.lang3.StringUtils
import org.junit.jupiter.api.*
import org.skellig.task.TaskUtils.Companion.runTask
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.tcp.model.TcpDetails
import org.slf4j.LoggerFactory
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal class TcpChannelTest {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(TcpChannelTest::class.java)
        private val DEFAULT_DATA = StringUtils.repeat("a", 512)
        private const val DEFAULT_BUFFER_SIZE = 1024
    }

    private var socketRequestHandlers = mutableListOf<SocketRequestHandler>()
    private var executorService = Executors.newCachedThreadPool()
    private var tcpChannel: TcpChannel? = null
    private var server: ServerSocket? = null

    @AfterEach
    fun tearDown() {
        executorService.shutdown()
        tcpChannel!!.close()
        socketRequestHandlers.forEach { it.close() }
        server!!.close()
    }

    @Test
    @DisplayName("Send and read When Server get request and responds only Then received response")
    fun testSendAndReadTwoTimes() {
        startSocketServer()
        tcpChannel!!.send(DEFAULT_DATA.toByteArray())

        var response = tcpChannel!!.read(1000, DEFAULT_BUFFER_SIZE)
        Assertions.assertEquals(DEFAULT_DATA, String((response as ByteArray?)!!))

        tcpChannel!!.send(DEFAULT_DATA.toByteArray())
        response = tcpChannel!!.read(1000, DEFAULT_BUFFER_SIZE)

        Assertions.assertEquals(DEFAULT_DATA, String((response as ByteArray?)!!))
    }

    @Test
    @DisplayName("Read once When Server responds only once Then response received")
    fun testRead() {
        startSocketServer(0, 1)

        val response = tcpChannel!!.read(4000, DEFAULT_BUFFER_SIZE)

        Assertions.assertEquals(DEFAULT_DATA, String((response as ByteArray?)!!))
    }

    @Test
    @DisplayName("Read once When times out Then response not received")
    fun testReadWhenTimedOut() {
        startSocketServer(200, 1)

        val response = runTask({
            try {
                tcpChannel!!.read(100, DEFAULT_BUFFER_SIZE)
            } catch (ex: TestStepProcessingException) {
                null
            }
        }, 100, 10, { it == null })
        Assertions.assertNull(response)
    }

    @Test
    @DisplayName("Read 2 times When first read times out Then last read receives response")
    fun testReadSeveralTimesWhenTimedOut() {
        startSocketServer(200, 1)

        var response = tcpChannel!!.read(100, DEFAULT_BUFFER_SIZE)
        Assertions.assertNull(response)

        response = tcpChannel!!.read(500, DEFAULT_BUFFER_SIZE)
        Assertions.assertEquals(DEFAULT_DATA, String((response as ByteArray?)!!))
    }

    @Test
    @DisplayName("Read 2 times When Server responds only once Then last read times out")
    fun testReadSeveralTimesWhenServerRespondedOnce() {
        startSocketServer(0, 1)

        var response = tcpChannel!!.read(0, DEFAULT_BUFFER_SIZE)
        Assertions.assertNotNull(response)

        response = runTask({
            try {
                tcpChannel!!.read(100, DEFAULT_BUFFER_SIZE)
            } catch (ex: TestStepProcessingException) {
                null
            }
        }, 100, 10, { it == null })
        Assertions.assertNull(response)
    }

    @Test
    @DisplayName(
        "Read 3 times with different timeouts When Server responds only 2 times with delay " +
                "Then verify receives 2 responses"
    )
    fun testReadSeveralTimesWhenServerRespondedTwiceAndDelay() {
        startSocketServer(300, 2)

        var response = tcpChannel!!.read(0, DEFAULT_BUFFER_SIZE)
        Assertions.assertNotNull(response)

        response = tcpChannel!!.read(100, DEFAULT_BUFFER_SIZE)
        Assertions.assertNull(response)

        response = tcpChannel!!.read(500, DEFAULT_BUFFER_SIZE)
        Assertions.assertNotNull(response)
    }

    @Test
    @DisplayName("Read once with small buffer When Server responds only once Then response whole received")
    fun testReadLargeDataAndMaxBufferIsLimited() {
        startSocketServer(0, 1)

        val response = tcpChannel!!.read(100, 32)

        Assertions.assertEquals(DEFAULT_DATA.length, String((response as ByteArray?)!!).length)
    }

    private fun startSocketServer(delay: Int = 0, respondTimes: Int = 0) {
        val countDownLatch = CountDownLatch(1)
        executorService.execute {
            try {
                server = ServerSocket(1116)
                countDownLatch.countDown()
                while (!executorService.isShutdown) {
                    try {
                        val socket = server!!.accept()
                        executorService.submit {
                            val socketRequestHandler = SocketRequestHandler(socket, delay, respondTimes)
                            socketRequestHandlers.add(socketRequestHandler)
                            socketRequestHandler.run()
                        }
                    } catch (ignored: Exception) {
                    }
                }
                LOGGER.debug("Shutting tcp server down...")
            } catch (ignored: Exception) {
            }
        }
        // wait a bit to startup the server
        countDownLatch.await(2, TimeUnit.SECONDS)
        tcpChannel = TcpChannel(TcpDetails("h1", "localhost", 1116))
    }

    private inner class SocketRequestHandler(
        private val socket: Socket,
        private val delay: Int,
        private var respondTimes: Int
    ) : Runnable {

        private val isRespondOnly: Boolean = respondTimes > 0

        override fun run() {
            try {
                DataInputStream(socket.getInputStream()).use { inStream ->
                    DataOutputStream(socket.getOutputStream()).use { out ->
                        LOGGER.debug("Connected to " + socket.remoteSocketAddress)

                        println("Connected to " + socket.remoteSocketAddress)

                        readAndRespond(inStream, out)
                    }
                }
            } catch (ex: Exception) {
                close()
            }
        }

        @Throws(Exception::class)
        private fun readAndRespond(inStream: DataInputStream, out: DataOutputStream) {
            while (!socket.isClosed && !executorService.isShutdown && respondTimes >= 0) {
                if (isRespondOnly) {
                    if (respondTimes-- > 0) {
                        respond(out, DEFAULT_DATA.toByteArray())
                    }
                } else {
                    var bytes = ByteArray(DEFAULT_DATA.length)

                    val read = inStream.read(bytes)
                    bytes = bytes.copyOf(read)

                    LOGGER.debug("Data read: " + String(bytes))
                    respond(out, bytes)
                }
            }
        }

        @Throws(InterruptedException::class, IOException::class)
        private fun respond(out: DataOutputStream, bytes: ByteArray) {
            if (delay > 0) {
                Thread.sleep(delay.toLong())
            }
            out.write(bytes, 0, bytes.size)
            out.flush()

            LOGGER.debug("Sent data: " + String(bytes))
        }

        fun close() {
            try {
                socket.close()
            } catch (ignored: Exception) {
            }
        }
    }

}