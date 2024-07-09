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
import org.skellig.teststep.processor.rmq.config.RmqTestStepProcessorConfig
import org.skellig.teststep.processor.rmq.model.RmqTestStep
import org.skellig.teststep.reader.value.expression.ValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.alphaNum
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.callChain
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.funcCall
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.list
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.map
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.num
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.string
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

@Tag("integration-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RmqTestProcessingIT {

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
        testStepProcessor.close()
        containers.values.parallelStream().forEach { it.close() }
    }

    @Test
    fun `send data to queue and read from it then verify response is correct`() {

        val rawTestStep: Map<ValueExpression, ValueExpression> = mapOf(
            Pair(alphaNum("protocol"), alphaNum("rmq")),
            Pair(alphaNum("sendTo"), list(string("queue1"))),
            Pair(alphaNum("routingKey"), string("#")),
            Pair(
                alphaNum("data"), map(
                    Pair(alphaNum("id"), callChain(num("1"), funcCall("toInt"))),
                    Pair(alphaNum("description"), alphaNum("test")),
                )
            )
        )
        assertTrue(testStepFactory.isConstructableFrom(rawTestStep), "Rmq Test Step must be constructable for its factory")
        val testStep = testStepFactory.create("t1", rawTestStep, emptyMap())

        testStepProcessor.process(testStep).subscribe { _, _, e ->
            assertEquals("", e?.message ?: "")
        }

        val validationTestStep = testStepFactory.create(
            "t2",
            mapOf(
                Pair(alphaNum("protocol"), alphaNum("rmq")),
                Pair(alphaNum("readFrom"), list(string("queue1"))),
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
    }

    private fun createContainer(version: String) =
        GenericContainer(DockerImageName.parse("rabbitmq:$version"))
            .withExposedPorts(5672)
}