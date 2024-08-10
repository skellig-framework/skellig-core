package org.skellig.teststep.processor.http.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.stubbing.Scenario
import com.typesafe.config.ConfigFactory
import org.junit.jupiter.api.*
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
import org.skellig.teststep.processor.http.config.HttpTestStepProcessorConfig
import org.skellig.teststep.processor.http.model.HttpTestStep
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
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
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.ref
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.string


@Tag("integration-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HttpTestStepProcessingIT {

    private lateinit var mockServers: List<WireMockServer>
    private lateinit var testStepProcessor: TestStepProcessor<HttpTestStep>
    private lateinit var testStepFactory: TestStepFactory<HttpTestStep>

    @BeforeAll
    fun setup() {
        val wireMockServer = WireMockServer(9192)
        wireMockServer.start()
        WireMock.configureFor("localhost", wireMockServer.port())

        val wireMockServer2 = WireMockServer(9193)
        wireMockServer2.start()
        WireMock.configureFor("localhost", wireMockServer2.port())

        mockServers = listOf(wireMockServer, wireMockServer2)

        init()
    }

    @AfterAll
    fun tearDown() {
        mockServers.forEach { it.stop() }
    }

    @Test
    fun `test get http request`() {
        mockServers.forEach {
            it.stubFor(
                WireMock.get(WireMock.urlEqualTo("/sample"))
                    .willReturn(
                        aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"message\": \"Hello, world!\"}")
                            .withStatus(200)
                    )
            )
        }

        val rawTestStep: Map<ValueExpression, ValueExpression> = mapOf(
            Pair(alphaNum("services"), alphaNum("service_a")),
            Pair(alphaNum("url"), alphaNum("/sample")),
            Pair(alphaNum("method"), alphaNum("GET")),
            Pair(
                alphaNum("headers"), map(Pair(string("Content-Type"), string("application/json")))
            ),

            Pair(
                alphaNum("validate"), map(
                    Pair(
                        alphaNum("service_a"), map(
                            Pair(
                                alphaNum("statusCode"),
                                callChain(num("200"), funcCall("toInt"))
                            ),
                            Pair(
                                callChain(alphaNum("headers"), string("Content-Type")),
                                string("application/json")
                            ),
                            Pair(
                                callChain(
                                    alphaNum("body"),
                                    funcCall("jsonPath", arrayOf(string("message")))
                                ),
                                string("Hello, world!")
                            )
                        )
                    )
                )
            )
        )
        Assertions.assertTrue(testStepFactory.isConstructableFrom(rawTestStep), "HTTP Test Step must be constructable for its factory")
        val testStep = testStepFactory.create(
            "t1",
            rawTestStep,
            emptyMap()
        )

        testStepProcessor.process(testStep).subscribe { _, _, e ->
            Assertions.assertEquals("", e?.message ?: "")
        }
    }

    @Test
    fun `test post http request`() {
        mockServers.forEach {
            it.stubFor(
                WireMock.post(WireMock.urlEqualTo("/post-endpoint"))
                    .withRequestBody(equalToJson("{\"key\": \"value\"}"))
                    .willReturn(
                        aResponse()
                            .withStatus(201)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"response\": \"Received\"}")
                    )
            )
        }

        val rawTestStep: Map<ValueExpression, ValueExpression> = mapOf(
            Pair(alphaNum("services"), list(alphaNum("service_a"), alphaNum("service_b"))),
            Pair(alphaNum("url"), alphaNum("/post-endpoint")),
            Pair(alphaNum("method"), alphaNum("POST")),

            // the payload will be converted to json String as it is set as default data converter in http config
            Pair(
                alphaNum("payload"), map(
                    Pair(alphaNum("key"), ref(alphaNum("ref_to_key")))
                )
            ),

            Pair(
                alphaNum("validate"), map(
                    Pair(
                        alphaNum("getValues"), list(
                            map(
                                Pair(
                                    alphaNum("statusCode"),
                                    callChain(num("201"), funcCall("toInt"))
                                ),
                                Pair(
                                    callChain(
                                        alphaNum("body"),
                                        funcCall("jsonPath", arrayOf(string("response")))
                                    ),
                                    string("Received")
                                )
                            )
                        )
                    )
                )
            )
        )
        val testStep = testStepFactory.create("t1", rawTestStep, mapOf(Pair("ref_to_key", "value")))

        testStepProcessor.process(testStep).subscribe { _, _, e ->
            Assertions.assertEquals("", e?.message ?: "")
        }
    }

    @Test
    fun `test get http request with delay`() {
        mockServers[0].stubFor(
            WireMock.get(WireMock.urlEqualTo("/delayed"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withFixedDelay(3000)
                        .withBody("{\"message\": \"Delayed response\"}")
                )
        )

        val rawTestStep: Map<ValueExpression, ValueExpression> = mapOf(
            Pair(alphaNum("services"), alphaNum("service_a")),
            Pair(alphaNum("timeout"), alphaNum("500")),
            Pair(alphaNum("url"), alphaNum("/delayed")),
            Pair(alphaNum("method"), alphaNum("GET")),
            Pair(
                alphaNum("validate"), map(
                    Pair(
                        alphaNum("service_a"), null
                    )
                )
            )
        )

        testStepProcessor.process(testStepFactory.create("t1", rawTestStep, emptyMap())).subscribe { _, r, e ->
            Assertions.assertEquals("", e?.message ?: "")
        }
    }

    @Test
    fun `test get http request from all services until get valid data from all`() {
        val scenarioName = "scenarioName"
        val scenarioName2 = "scenarioName2"
        mockServers[0].stubFor(
            WireMock.get(WireMock.urlEqualTo("/test-endpoint"))
                .inScenario(scenarioName)
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withFixedDelay(3000)
                        .withBody("Delayed response")
                )
                .willSetStateTo("Failed Once")
        )

        mockServers[0].stubFor(
            WireMock.get(WireMock.urlEqualTo("/test-endpoint"))
                .inScenario(scenarioName)
                .whenScenarioStateIs("Failed Once")
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withFixedDelay(1)
                        .withBody("Successful response")
                )
                .willSetStateTo("Success Once")
        )

        // simulate last timeout to verify that previously successful result is used
        mockServers[0].stubFor(
            WireMock.get(WireMock.urlEqualTo("/test-endpoint"))
                .inScenario(scenarioName)
                .whenScenarioStateIs("Success Once")
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withFixedDelay(3000)
                        .withBody("Second Delayed response")
                )
        )

        mockServers[1].stubFor(
            WireMock.get(WireMock.urlEqualTo("/test-endpoint"))
                .inScenario(scenarioName2)
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(
                    aResponse()
                        .withStatus(500)
                        .withFixedDelay(1)
                        .withBody("Error")
                )
                .willSetStateTo("Failed Once")
        )

        mockServers[1].stubFor(
            WireMock.get(WireMock.urlEqualTo("/test-endpoint"))
                .inScenario(scenarioName2)
                .whenScenarioStateIs("Failed Once")
                .willReturn(
                    aResponse()
                        .withStatus(500)
                        .withFixedDelay(1)
                        .withBody("Error")
                )
                .willSetStateTo("Failed Twice")
        )

        mockServers[1].stubFor(
            WireMock.get(WireMock.urlEqualTo("/test-endpoint"))
                .inScenario(scenarioName2)
                .whenScenarioStateIs("Failed Twice")
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withFixedDelay(1)
                        .withBody("Successful response")
                )
        )

        val rawTestStep: Map<ValueExpression, ValueExpression> = mapOf(
            Pair(alphaNum("services"), list(alphaNum("service_a"), alphaNum("service_b"))),
            Pair(alphaNum("timeout"), alphaNum("1500")),
            Pair(alphaNum("delay"), alphaNum("50")),
            Pair(alphaNum("attempts"), alphaNum("3")),
            Pair(alphaNum("url"), alphaNum("/test-endpoint")),
            Pair(alphaNum("method"), alphaNum("GET")),
            Pair(
                alphaNum("validate"), map(
                    Pair(compare("!=", alphaNum("service_a"), AlphanumericValueExpression("null")), bool("true")),
                    Pair(callChain(alphaNum("service_a"), alphaNum("statusCode")), callChain(num("200"), funcCall("toInt"))),
                    Pair(callChain(alphaNum("service_b"), alphaNum("statusCode")), callChain(num("200"), funcCall("toInt"))),
                    Pair(callChain(alphaNum("service_b"), alphaNum("body")), string("Successful response")),
                )
            )
        )

        val testStep = testStepFactory.create("t1", rawTestStep, emptyMap())
        testStepProcessor.process(testStep).subscribe { _, _, e ->
            Assertions.assertEquals("", e?.message ?: "")
        }
    }

    private fun init() {
        val state = DefaultTestScenarioState()
        // some dependencies are mocked because they won't be used in the test
        val config = HttpTestStepProcessorConfig().config(
            TestStepProcessorConfigDetails(
                state,
                ConfigFactory.load(this.javaClass.classLoader, "http-integration-test.conf"),
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
}