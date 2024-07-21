package org.skellig.teststep.processor.ibmmq.integration

import com.typesafe.config.ConfigFactory
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
import org.skellig.teststep.processor.ibmmq.config.IbmMqConsumableTestStepProcessorConfig
import org.skellig.teststep.processor.ibmmq.config.IbmMqTestStepProcessorConfig
import org.skellig.teststep.processor.ibmmq.model.IbmMqConsumableTestStep
import org.skellig.teststep.processor.ibmmq.model.IbmMqTestStep
import org.skellig.teststep.reader.value.expression.ValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.alphaNum
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.bool
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.boolOp
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.callChain
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.compare
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.funcCall
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.list
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.map
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.num
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.string
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Tag("integration-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class IbmMqConsumableTestProcessingIT {

    private lateinit var testStepConsumableProcessor: TestStepProcessor<IbmMqConsumableTestStep>
    private lateinit var testStepConsumableFactory: TestStepFactory<IbmMqConsumableTestStep>
    private lateinit var testStepProcessor: TestStepProcessor<IbmMqTestStep>
    private lateinit var testStepFactory: TestStepFactory<IbmMqTestStep>
    private val versions = arrayOf("9.2.3.0-r1")
    private val containers = versions.associateWith { createContainer(it) }

    @BeforeAll
    fun setUp() {
        containers.values.parallelStream().forEach { it.start() }
        init()
    }

    @AfterAll
    fun tearDown() {
        testStepProcessor.close()
        testStepConsumableProcessor.close()
        containers.values.parallelStream().forEach { it.close() }
    }

    @Test
    fun `send data to 2 queues and consume from them then verify response is correct`() {
        val n = 5
        (1..n).forEach {
            val rawTestStep: Map<ValueExpression, ValueExpression> = mapOf(
                Pair(alphaNum("protocol"), alphaNum("ibmmq")),
                Pair(alphaNum("sendTo"), list(string("DEV.QUEUE.1"), string("DEV.QUEUE.2"))),
                Pair(
                    alphaNum("data"), map(
                        Pair(alphaNum("id"), callChain(num(it.toString()), funcCall("toInt"))),
                    )
                )
            )
            val testStep = testStepFactory.create("t1", rawTestStep, emptyMap())

            testStepProcessor.process(testStep)
        }

        val rawTestStep = mapOf<ValueExpression, ValueExpression>(
            Pair(alphaNum("protocol"), alphaNum("ibmmq")),
            Pair(alphaNum("timeout"), num("200")),
            Pair(alphaNum("consumeFrom"), list(string("DEV.QUEUE.1"), string("DEV.QUEUE.2"))),
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

        val countDownLatch = CountDownLatch(2 * n)
        testStepConsumableProcessor.process(testStepConsumableFactory.create("t2", rawTestStep, emptyMap()))
            .subscribe { _, _, e ->
                assertEquals("", e?.message ?: "")
                countDownLatch.countDown()
            }

        countDownLatch.await(5, TimeUnit.SECONDS)

        assertTrue(testStepConsumableFactory.isConstructableFrom(rawTestStep), "Consumable IbmMq Test Step must be constructable for its factory")
        assertEquals(0, countDownLatch.count, "Not all valid data received from the consumer")
    }

    @Test
    fun `consume from queue and respond to another queue`() {
        val expectedValue = "10000"
        testStepProcessor.process(testStepFactory.create(
            "t1", mapOf(
                Pair(alphaNum("protocol"), alphaNum("ibmmq")),
                Pair(alphaNum("sendTo"), list(string("DEV.QUEUE.1"))),
                Pair(
                    alphaNum("data"), map(
                        Pair(alphaNum("id"), callChain(num(expectedValue), funcCall("toInt"))),
                    )
                )
            ), emptyMap()
        ))

        val consumeTestStep = testStepConsumableFactory.create(
            "t2", mapOf(
                Pair(alphaNum("protocol"), alphaNum("ibmmq")),
                Pair(alphaNum("timeout"), num("1000")),
                Pair(alphaNum("consumeFrom"), list(string("DEV.QUEUE.1"))),
                Pair(alphaNum("respondTo"), list(string("DEV.QUEUE.3"))),
                Pair(
                    alphaNum("data"), map(
                        Pair(alphaNum("id"), callChain(num("-$expectedValue"), funcCall("toInt"))),
                    )
                ),
                Pair(
                    alphaNum("validate"), map(
                        Pair(
                            compare("!=", alphaNum("$"), alphaNum("null")),
                            bool("true")
                        )
                    )
                )
            ), emptyMap()
        )

        val countDownLatch = CountDownLatch(1)
        testStepConsumableProcessor.process(consumeTestStep)
            .subscribe { _, _, e ->
                assertEquals("", e?.message ?: "")
                countDownLatch.countDown()
            }

        countDownLatch.await(10, TimeUnit.SECONDS)

        testStepProcessor.process(testStepFactory.create(
            "t3", mapOf(
                Pair(alphaNum("protocol"), alphaNum("ibmmq")),
                Pair(alphaNum("timeout"), num("5000")),
                Pair(alphaNum("attempts"), num("3")),
                Pair(alphaNum("readFrom"), list(string("DEV.QUEUE.3"))),
                Pair(
                    alphaNum("validate"), map(
                        Pair(
                            callChain(string("DEV.QUEUE.3"), funcCall("toString"), funcCall("jsonPath", arrayOf(alphaNum("id")))),
                            num("-$expectedValue")
                        )
                    )
                )
            ), emptyMap()
        )) .subscribe { _, _, e ->
            assertEquals("", e?.message ?: "")
        }

        assertEquals(0, countDownLatch.count, "Not all valid data received from the consumer")
    }

    private fun init() {
        val state = DefaultTestScenarioState()
        // some dependencies are mocked because they won't be used in the test
        val config = IbmMqTestStepProcessorConfig().config(
            TestStepProcessorConfigDetails(
                state,
                ConfigFactory.load(this.javaClass.classLoader, "ibmmq-integration-test.conf"),
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

        val config2 = IbmMqConsumableTestStepProcessorConfig().config(
            TestStepProcessorConfigDetails(
                state,
                ConfigFactory.load(this.javaClass.classLoader, "ibmmq-integration-test.conf"),
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

    private fun createContainer(version: String) =
        GenericContainer(DockerImageName.parse("ibmcom/mq:$version"))
            .withExposedPorts(1414)
            .withExposedPorts(9443)
            .withEnv("LICENSE", "accept")
            .withEnv("MQ_QMGR_NAME", "QM1")
            .withEnv("MQ_APP_PASSWORD", "admin")
}