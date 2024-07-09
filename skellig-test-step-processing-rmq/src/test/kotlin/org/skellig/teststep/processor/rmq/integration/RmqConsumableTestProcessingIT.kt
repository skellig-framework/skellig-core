package org.skellig.teststep.processor.rmq.integration

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
import org.skellig.teststep.processor.rmq.config.RmqConsumableTestStepProcessorConfig
import org.skellig.teststep.processor.rmq.config.RmqTestStepProcessorConfig
import org.skellig.teststep.processor.rmq.model.RmqConsumableTestStep
import org.skellig.teststep.processor.rmq.model.RmqTestStep
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
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Tag("integration-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RmqConsumableTestProcessingIT {

    private lateinit var testStepConsumableProcessor: TestStepProcessor<RmqConsumableTestStep>
    private lateinit var testStepConsumableFactory: TestStepFactory<RmqConsumableTestStep>
    private lateinit var testStepProcessor: TestStepProcessor<RmqTestStep>
    private lateinit var testStepFactory: TestStepFactory<RmqTestStep>
    private val versions = arrayOf(/*"3.5.0", */"3.13.3")
    private val containers = versions.associateWith { createContainer(it) }

    @BeforeAll
    fun setUp() {
        containers.values.parallelStream().forEach { it.start() }
        init()
    }

    @AfterAll
    fun tearDown() {
        testStepConsumableProcessor.close()
        containers.values.parallelStream().forEach { it.close() }
    }

    @Test
    fun `send data to 2 queues and consume from them then verify response is correct`() {
        val n = 5
        (1..n).forEach {
            val rawTestStep: Map<ValueExpression, ValueExpression> = mapOf(
                Pair(alphaNum("protocol"), alphaNum("rmq")),
                Pair(alphaNum("sendTo"), list(string("queue1"), string("queue2"))),
                Pair(alphaNum("routingKey"), string("#")),
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
            Pair(alphaNum("protocol"), alphaNum("rmq")),
            Pair(alphaNum("consumeFrom"), list(string("queue1"), string("queue2"))),
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

        assertTrue(testStepConsumableFactory.isConstructableFrom(rawTestStep), "Consumable Rmq Test Step must be constructable for its factory")
        assertEquals(0, countDownLatch.count, "Not all valid data received from the consumer")
    }

    @Test
    fun `consume from queue and respond to another queue`() {
        val expectedValue = "10000"
        testStepProcessor.process(testStepFactory.create(
            "t1", mapOf(
                Pair(alphaNum("protocol"), alphaNum("rmq")),
                Pair(alphaNum("sendTo"), list(string("queue1"))),
                Pair(alphaNum("routingKey"), string("#")),
                Pair(
                    alphaNum("data"), map(
                        Pair(alphaNum("id"), callChain(num(expectedValue), funcCall("toInt"))),
                    )
                )
            ), emptyMap()
        ))

        val consumeTestStep = testStepConsumableFactory.create(
            "t2", mapOf(
                Pair(alphaNum("protocol"), alphaNum("rmq")),
                Pair(alphaNum("consumeFrom"), list(string("queue1"))),
                Pair(alphaNum("respondTo"), list(string("queue2"))),
                Pair(
                    alphaNum("response"), map(
                        Pair(alphaNum("id"), callChain(num("-$expectedValue"), funcCall("toInt"))),
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

        countDownLatch.await(3, TimeUnit.SECONDS)

        testStepProcessor.process(testStepFactory.create(
            "t1", mapOf(
                Pair(alphaNum("protocol"), alphaNum("rmq")),
                Pair(alphaNum("readFrom"), list(string("queue2"))),
                Pair(alphaNum("routingKey"), string("#")),
                Pair(
                    alphaNum("validate"), map(
                        Pair(
                            callChain(alphaNum("queue2"), funcCall("toString"), funcCall("jsonPath", arrayOf(alphaNum("id")))),
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
        val config = RmqTestStepProcessorConfig().config(
            TestStepProcessorConfigDetails(
                state,
                ConfigFactory.load(this.javaClass.classLoader, "rmq-integration-test.conf"),
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

        val config2 = RmqConsumableTestStepProcessorConfig().config(
            TestStepProcessorConfigDetails(
                state,
                ConfigFactory.load(this.javaClass.classLoader, "rmq-integration-test.conf"),
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
        GenericContainer(DockerImageName.parse("rabbitmq:$version"))
            .withExposedPorts(5672)
}