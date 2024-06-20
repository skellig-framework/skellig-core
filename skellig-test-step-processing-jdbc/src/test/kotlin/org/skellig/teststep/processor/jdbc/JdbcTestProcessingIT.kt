package org.skellig.teststep.processor.jdbc

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
import org.skellig.teststep.processor.jdbc.config.JdbcTestStepProcessorConfig
import org.skellig.teststep.processor.jdbc.model.JdbcTestStep
import org.skellig.teststep.reader.value.expression.ValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.alphaNum
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.bool
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.callChain
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.funcCall
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.list
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.map
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.num
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.ref
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.string
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

private const val DEFAULT_PORT = 5432

@Tag("integration-test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class JdbcTestProcessingIT {

    companion object {
        private const val TABLE = "skellig_info"
        private const val USER_NAME = "skellig"
        private const val PASSWORD = "skellig"
    }

    private lateinit var testStepProcessor: TestStepProcessor<JdbcTestStep>
    private lateinit var testStepFactory: TestStepFactory<JdbcTestStep>
    private val versions = arrayOf("12.18", "13.14", "14.11", "15.6", "16.2")
    private val containers = versions.associateWith { createContainer(it) }

    @BeforeAll
    fun setUpDb() {
        containers.values.parallelStream().forEach { it.start() }
        init()
    }

    @AfterAll
    fun tearDown() {
        testStepProcessor.close()
        containers.values.parallelStream().forEach { it.close() }
    }

    @Test
    fun `insert record in the table and verify with select command`() {

        val rawTestStep: Map<ValueExpression, ValueExpression> = mapOf(
            Pair(alphaNum("provider"), alphaNum("jdbc")),
            Pair(alphaNum("servers"), list(versions.map { string("srv_$it") }.toList())),
            Pair(alphaNum("table"), string(TABLE)),
            Pair(alphaNum("command"), alphaNum("insert")),
            Pair(
                alphaNum("values"), map(
                    Pair(
                        alphaNum("date_created"),
                        callChain(
                            funcCall("now"),
                            funcCall("toLocalDate")
                        )
                    ),
                )
            ),
            Pair(
                alphaNum("data"), map(
                    Pair(alphaNum("id"), callChain(num("1"), funcCall("toInt"))),
                    Pair(alphaNum("create_date"), ref("date_created")),
                    Pair(alphaNum("description"), alphaNum("test")),
                )
            ),
            Pair(
                alphaNum("state"), map(
                    Pair(alphaNum("date_created"), ref("date_created")),
                )
            )
        )
        Assertions.assertTrue(testStepFactory.isConstructableFrom(rawTestStep), "Jdbc Test Step must be constructable for its factory")
        val testStep = testStepFactory.create(
            "t1",
            rawTestStep,
            emptyMap()
        )

        testStepProcessor.process(testStep).subscribe { _, _, e ->
            Assertions.assertEquals("", e?.message ?: "")
        }

        val validationTestStep = testStepFactory.create(
            "t2",
            mapOf(
                Pair(alphaNum("provider"), alphaNum("jdbc")),
                Pair(alphaNum("servers"), list(versions.map { string("srv_$it") })),
                Pair(alphaNum("table"), string(TABLE)),
                Pair(alphaNum("command"), alphaNum("select")),
                Pair(
                    alphaNum("validate"), map(
                        Pair(
                            // getValues returns a list of records (list) for each DB server
                            funcCall("getValues"),
                            list(
                                list(
                                    listOf(
                                        map(
                                            Pair(alphaNum("id"), callChain(num("1"), funcCall("toInt"))),
                                            Pair(
                                                callChain(
                                                    alphaNum("create_date"),
                                                    funcCall(
                                                        "between", arrayOf(
                                                            string("now"),
                                                            string("now"),
                                                        )
                                                    )
                                                ),
                                                bool("true")
                                            ),
                                            Pair(alphaNum("description"), alphaNum("test")),
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
                Assertions.assertEquals("", e?.message ?: "")
            }
    }

    @Test
    fun `update record in the keyspace and verify with select`() {
        val testStep = testStepFactory.create(
            "t1",
            mapOf(
                Pair(alphaNum("provider"), alphaNum("jdbc")),
                Pair(alphaNum("servers"), list(versions.map { string("srv_$it") })),
                Pair(
                    alphaNum("query"),
                    alphaNum("insert INTO skellig_info (id,create_date,description) VALUES(2,'10.10.2024','test')")
                )
            ),
            emptyMap()
        )

        testStepProcessor.process(testStep).subscribe { _, _, e ->
            Assertions.assertEquals("", e?.message ?: "")
        }

        val updateTestStep2 = testStepFactory.create(
            "t2",
            mapOf(
                Pair(alphaNum("provider"), alphaNum("jdbc")),
                Pair(alphaNum("servers"), list(versions.map { string("srv_$it") })),
                Pair(alphaNum("table"), string(TABLE)),
                Pair(alphaNum("command"), alphaNum("update")),
                Pair(
                    alphaNum("data"), map(
                        Pair(alphaNum("description"), alphaNum("new test description")),
                        Pair(
                            alphaNum("where"), map(
                                Pair(alphaNum("id"), callChain(num("2"), funcCall("toInt"))),
                            )
                        ),
                    )
                )
            ),
            emptyMap()
        )
        testStepProcessor.process(updateTestStep2)
            .subscribe { _, _, e ->
                Assertions.assertEquals("", e?.message ?: "")
            }

        val validationTestStep = testStepFactory.create(
            "t3",
            mapOf(
                Pair(alphaNum("provider"), alphaNum("cassandra")),
                Pair(alphaNum("servers"), string("srv_${versions[2]}")),
                Pair(
                    alphaNum("query"),
                    string("SELECT description FROM skellig_info WHERE create_date = '10.10.2024'")
                ),
                Pair(
                    alphaNum("validate"), map(
                        Pair(
                            string("srv_${versions[2]}"),
                            map(
                                Pair(
                                    funcCall("size"),
                                    callChain(num("1"), funcCall("toInt"))
                                ),
                                Pair(
                                    callChain(funcCall("first"), alphaNum("description")),
                                    alphaNum("new test description")
                                ),
                            )
                        )
                    )
                )
            ),
            emptyMap()
        )
        testStepProcessor.process(validationTestStep)
            .subscribe { _, _, e ->
                Assertions.assertEquals("", e?.message ?: "")
            }

    }

    private fun init() {
        containers.forEach { System.setProperty("container_url_${it.key}", it.value.jdbcUrl) }

        val state = DefaultTestScenarioState()
        // some dependencies are mocked because they won't be used in the test
        val config = JdbcTestStepProcessorConfig().config(
            TestStepProcessorConfigDetails(
                state,
                ConfigFactory.load(this.javaClass.classLoader, "jdbc-integration-test.conf"),
                mock<TestStepRegistry>(),
                ValueExpressionContextFactory(
                    DefaultFunctionValueExecutor.Builder()
                        .withTestScenarioState(state)
                        .withClassLoader(JdbcTestProcessingIT::class.java.classLoader)
                        .build(), DefaultPropertyExtractor(null)
                ),
                mock<TestStepProcessor<TestStep>>(),
                mock<TestStepFactory<TestStep>>(),
            )
        )!!
        testStepFactory = config.testStepFactory
        testStepProcessor = config.testStepProcessor
    }

    private fun createContainer(version: String) = PostgreSQLContainer("postgres:$version")
        .withDatabaseName("skellig")
        .withUsername(USER_NAME)
        .withPassword(PASSWORD)
        .withExposedPorts(DEFAULT_PORT)
        .withClasspathResourceMapping(
            "sqls/test/init.sql",
            "/docker-entrypoint-initdb.d/init-postgres.sql", BindMode.READ_ONLY
        )
        .withReuse(true)
}