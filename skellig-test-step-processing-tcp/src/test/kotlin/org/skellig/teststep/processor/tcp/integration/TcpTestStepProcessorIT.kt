package org.skellig.teststep.processor.tcp.integration

import com.typesafe.config.ConfigFactory
import org.apache.commons.lang3.StringUtils
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.kotlin.mock
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.TestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processing.value.function.DefaultFunctionValueExecutor
import org.skellig.teststep.processing.value.property.DefaultPropertyExtractor
import org.skellig.teststep.processor.tcp.config.TcpConsumableTestStepProcessorConfig
import org.skellig.teststep.processor.tcp.config.TcpTestStepProcessorConfig
import org.skellig.teststep.processor.tcp.model.TcpConsumableTestStep
import org.skellig.teststep.processor.tcp.model.TcpTestStep
import org.skellig.teststep.reader.value.expression.ValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.alphaNum
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.bool
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.callChain
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.compare
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.funcCall
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.list
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.map
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.num
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.string
import org.slf4j.LoggerFactory
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Tag("integration-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TcpTestStepProcessorIT {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(TcpTestStepProcessorIT::class.java)
        private val DEFAULT_DATA = StringUtils.repeat("a", 512)
    }

    private lateinit var testStepConsumableProcessor: TestStepProcessor<TcpConsumableTestStep>
    private lateinit var testStepConsumableFactory: TestStepFactory<TcpConsumableTestStep>
    private lateinit var testStepProcessor: TestStepProcessor<TcpTestStep>
    private lateinit var testStepFactory: TestStepFactory<TcpTestStep>
    private lateinit var executorService: ExecutorService
    private lateinit var socketRequestHandlers: MutableList<SocketRequestHandler>
    private lateinit var servers: MutableList<ServerSocket>

    @BeforeAll
    fun setUp() {
        executorService = Executors.newCachedThreadPool()
        socketRequestHandlers = mutableListOf()
        servers = mutableListOf()
        startSocketServer()
        init()
    }


    @AfterAll
    fun tearDown() {
        socketRequestHandlers.clear()
        testStepConsumableProcessor.close()
        testStepProcessor.close()
        servers.forEach { it.close() }
        executorService.shutdownNow()
        executorService.awaitTermination(10, TimeUnit.SECONDS)
    }

    @Test
    fun `send data to host and read from it then verify response is correct`() {
        val rawTestStep: Map<ValueExpression, ValueExpression> = mapOf(
            Pair(alphaNum("protocol"), alphaNum("tcp")),
            Pair(alphaNum("sendTo"), list(string("host2"))),
            Pair(
                alphaNum("data"), map(
                    Pair(alphaNum("id"), callChain(num("1"), funcCall("toInt"))),
                    Pair(alphaNum("description"), alphaNum("test")),
                )
            )
        )
        assertTrue(testStepFactory.isConstructableFrom(rawTestStep), "TCP Test Step must be constructable for its factory")
        val testStep = testStepFactory.create("t1", rawTestStep, emptyMap())

        testStepProcessor.process(testStep).subscribe { _, _, e ->
            assertEquals("", e?.message ?: "")
        }

        val validationTestStep = testStepFactory.create(
            "t2",
            mapOf(
                Pair(alphaNum("protocol"), alphaNum("tcp")),
                Pair(alphaNum("readFrom"), list(string("host2"))),
                Pair(
                    alphaNum("validate"), map(
                        Pair(
                            funcCall("getValues"),
                            list(
                                list(
                                    listOf(
                                        map(
                                            Pair(callChain(funcCall("toString"), funcCall("jsonPath", arrayOf(alphaNum("id")))), callChain(num("1"), funcCall("toInt"))),
                                            Pair(callChain(funcCall("toString"), funcCall("jsonPath", arrayOf(alphaNum("description")))), alphaNum("test"))
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            emptyMap()
        )
        testStepProcessor.process(validationTestStep)
            .subscribe { _, _, e ->
                assertEquals("", e?.message ?: "")
            }
    }

    @Test
    fun `send data to 2 queues and consume from them then verify response is correct`() {
        val rawTestStep = mapOf<ValueExpression, ValueExpression>(
            Pair(alphaNum("protocol"), alphaNum("tcp")),
            Pair(alphaNum("timeout"), num("2000")),
            Pair(alphaNum("consumeFrom"), list(string("host1"))),
            Pair(
                alphaNum("validate"), map(
                    Pair(
                        compare(
                            "<=",
                            callChain(alphaNum("$"), funcCall("toString"), funcCall("jsonPath", arrayOf(alphaNum("id")))),
                            num("5")
                        ),
                        bool("true")
                    )
                )
            )
        )

        val n = 5
        val countDownLatch = CountDownLatch(n)
        testStepConsumableProcessor.process(testStepConsumableFactory.create("t2", rawTestStep, emptyMap()))
            .subscribe { _, _, e ->
                assertEquals("", e?.message ?: "")
                countDownLatch.countDown()
            }

        (1..n).forEach {
            val sendDataTestStep: Map<ValueExpression, ValueExpression> = mapOf(
                Pair(alphaNum("protocol"), alphaNum("tcp")),
                Pair(alphaNum("sendTo"), list(string("host1"))),
                Pair(
                    alphaNum("data"), map(
                        Pair(alphaNum("id"), callChain(num(it.toString()), funcCall("toInt"))),
                    )
                )
            )
            val testStep = testStepFactory.create("t1", sendDataTestStep, emptyMap())

            testStepProcessor.process(testStep)
        }

        countDownLatch.await(5, TimeUnit.SECONDS)

        assertTrue(testStepConsumableFactory.isConstructableFrom(rawTestStep), "Consumable TCO Test Step must be constructable for its factory")
        assertEquals(0, countDownLatch.count, "Not all valid data received from the consumer")
    }

    private fun init() {
        val state = DefaultTestScenarioState()
        // some dependencies are mocked because they won't be used in the test
        val config = TcpTestStepProcessorConfig().config(
            TestStepProcessorConfigDetails(
                state,
                ConfigFactory.load(this.javaClass.classLoader, "tcp-integration-test.conf"),
                mock<TestStepRegistry>(),
                ValueExpressionContextFactory(
                    DefaultFunctionValueExecutor.Builder()
                        .withTestScenarioState(state)
                        .withClassLoader(this.javaClass.classLoader)
                        .build(), DefaultPropertyExtractor(null)
                ),
                mock<TestStepProcessor<TestStep>>(),
                mock<TestStepFactory<TestStep>>(),
            )
        )!!
        testStepFactory = config.testStepFactory
        testStepProcessor = config.testStepProcessor

        val config2 = TcpConsumableTestStepProcessorConfig().config(
            TestStepProcessorConfigDetails(
                state,
                ConfigFactory.load(this.javaClass.classLoader, "tcp-integration-test.conf"),
                mock<TestStepRegistry>(),
                ValueExpressionContextFactory(
                    DefaultFunctionValueExecutor.Builder()
                        .withTestScenarioState(state)
                        .withClassLoader(this.javaClass.classLoader)
                        .build(), DefaultPropertyExtractor(null)
                ),
                mock<TestStepProcessor<TestStep>>(),
                mock<TestStepFactory<TestStep>>(),
            )
        )!!
        testStepConsumableFactory = config2.testStepFactory
        testStepConsumableProcessor = config2.testStepProcessor
    }

    private fun startSocketServer(delay: Int = 0, respondTimes: Int = 0) {
        val countDownLatch = CountDownLatch(2)
        (1116..1117).forEach {
            executorService.execute {
                try {
                    val server = ServerSocket(it)
                    servers.add(server)
                    while (!executorService.isShutdown) {
                        try {
                            val socket = server.accept()
                            executorService.submit {
                                val socketRequestHandler = SocketRequestHandler(socket, delay, respondTimes)
                                socketRequestHandlers.add(socketRequestHandler)
                                socketRequestHandler.run()
                            }
                        } catch (ignore: Exception) {
                        } finally {
                            countDownLatch.countDown()
                        }
                    }
                    LOGGER.debug("Shutting tcp server down...")
                } catch (ignored: Exception) {
                }
            }
        }

        // wait a bit to startup the server
        countDownLatch.await(2, TimeUnit.SECONDS)
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