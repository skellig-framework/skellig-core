package org.skellig.teststep.processor.cassandra.integration

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
import org.skellig.teststep.processor.cassandra.config.CassandraTestStepProcessorConfig
import org.skellig.teststep.processor.cassandra.model.CassandraTestStep
import org.skellig.teststep.reader.value.expression.*
import org.testcontainers.containers.CassandraContainer
import org.testcontainers.junit.jupiter.Testcontainers

@Tag("integration-test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CassandraTestProcessingIT {

    companion object {
        internal const val TABLE = "books.book"
    }

    private val cassandraContainers =
        listOf(
            createContainer("3.0"),
            createContainer("4.0.12"),
            createContainer("5.0")
        )

    private var processingDetails: List<Pair<TestStepFactory<CassandraTestStep>, TestStepProcessor<CassandraTestStep>>>? = null

    @BeforeAll
    fun setUpDb() {
        cassandraContainers.parallelStream().forEach { it.start() }
        initDatabase()
    }

    @AfterAll
    fun tearDown() {
        processingDetails!!.parallelStream().forEach { it.second.close() }
        cassandraContainers.parallelStream().forEach { it.close() }
    }

    @Test
    fun `insert record in the keyspace and verify with select command`() {
        processingDetails!!.parallelStream().forEach { processing ->
            val factory = processing.first
            val processor = processing.second

            val rawTestStep: Map<ValueExpression, ValueExpression> = mapOf(
                Pair(AlphanumericValueExpression("provider"), AlphanumericValueExpression("cassandra")),
                Pair(AlphanumericValueExpression("table"), StringValueExpression(TABLE)),
                Pair(AlphanumericValueExpression("command"), AlphanumericValueExpression("insert")),
                Pair(
                    AlphanumericValueExpression("values"), MapValueExpression(
                        mapOf(
                            Pair(
                                AlphanumericValueExpression("date_created"),
                                CallChainExpression(
                                    listOf(
                                        FunctionCallExpression("now"),
                                        FunctionCallExpression("toInstant", arrayOf(StringValueExpression("+0"), AlphanumericValueExpression("Millis")))
                                    )
                                )
                            ),
                        )
                    )
                ),
                Pair(
                    AlphanumericValueExpression("data"), MapValueExpression(
                        mapOf(
                            Pair(AlphanumericValueExpression("id"), CallChainExpression(listOf(NumberValueExpression("1"), FunctionCallExpression("toInt")))),
                            Pair(AlphanumericValueExpression("date_created"), PropertyValueExpression("date_created")),
                            Pair(AlphanumericValueExpression("name"), AlphanumericValueExpression("test")),
                        )
                    )
                ),
                Pair(
                    AlphanumericValueExpression("state"), MapValueExpression(
                        mapOf(
                            Pair(AlphanumericValueExpression("date_created"), PropertyValueExpression("date_created")),
                        )
                    )
                )
            )
            assertTrue(factory.isConstructableFrom(rawTestStep), "Cassandra Test Step must be constructable for its factory")
            val testStep = factory.create(
                "t1",
                rawTestStep,
                emptyMap()
            )

            processor.process(testStep).subscribe { _, _, e ->
                assertEquals("", e?.message ?: "")
            }

            val validationTestStep = factory.create(
                "t2",
                mapOf(
                    Pair(AlphanumericValueExpression("provider"), AlphanumericValueExpression("cassandra")),
                    Pair(AlphanumericValueExpression("table"), StringValueExpression(TABLE)),
                    Pair(AlphanumericValueExpression("command"), AlphanumericValueExpression("select")),
                    Pair(
                        AlphanumericValueExpression("validate"), MapValueExpression(
                            mapOf(
                                Pair(
                                    FunctionCallExpression("first"), MapValueExpression(
                                        mapOf(
                                            Pair(AlphanumericValueExpression("id"), CallChainExpression(listOf(NumberValueExpression("1"), FunctionCallExpression("toInt")))),
                                            Pair(
                                                CallChainExpression(
                                                    listOf(
                                                        AlphanumericValueExpression("date_created"),
                                                        FunctionCallExpression(
                                                            "between", arrayOf(
                                                                CallChainExpression(
                                                                    listOf(
                                                                        FunctionCallExpression("get", arrayOf(AlphanumericValueExpression("date_created"))),
                                                                        FunctionCallExpression("toDateTime"),
                                                                        FunctionCallExpression(
                                                                            "minusSeconds", arrayOf(
                                                                                CallChainExpression(
                                                                                    listOf(
                                                                                        NumberValueExpression("1"),
                                                                                        FunctionCallExpression("toLong")
                                                                                    )
                                                                                )
                                                                            )
                                                                        ),
                                                                    )
                                                                ),
                                                                CallChainExpression(
                                                                    listOf(
                                                                        FunctionCallExpression("get", arrayOf(AlphanumericValueExpression("date_created"))),
                                                                        FunctionCallExpression("toDateTime"),
                                                                        FunctionCallExpression(
                                                                            "plusSeconds", arrayOf(
                                                                                CallChainExpression(
                                                                                    listOf(
                                                                                        NumberValueExpression("1"),
                                                                                        FunctionCallExpression("toLong")
                                                                                    )
                                                                                )
                                                                            )
                                                                        ),
                                                                    )
                                                                )
                                                            )
                                                        )
                                                    )
                                                ),
                                                BooleanValueExpression("true")
                                            ),
                                            Pair(AlphanumericValueExpression("name"), AlphanumericValueExpression("test")),
                                        )
                                    )
                                )
                            )
                        )
                    )
                ),
                emptyMap()
            )
            processor.process(validationTestStep)
                .subscribe { _, _, e ->
                    assertEquals("", e?.message ?: "")
                }
        }
    }

    @Test
    fun `update record in the keyspace and verify with select`() {
        val processing = processingDetails!!.last()

        val factory = processing.first
        val processor = processing.second

        val testStep = factory.create(
            "t1",
            mapOf(
                Pair(AlphanumericValueExpression("provider"), AlphanumericValueExpression("cassandra")),
                Pair(
                    AlphanumericValueExpression("query"),
                    AlphanumericValueExpression("insert INTO books.book (id,date_created,name) VALUES(2,1577873400,'test')")
                )
            ),
            emptyMap()
        )

        processor.process(testStep).subscribe { _, _, e ->
            assertEquals("", e?.message ?: "")
        }

        val updateTestStep2 = factory.create(
            "t2",
            mapOf(
                Pair(AlphanumericValueExpression("provider"), AlphanumericValueExpression("cassandra")),
                Pair(AlphanumericValueExpression("table"), StringValueExpression(TABLE)),
                Pair(AlphanumericValueExpression("command"), AlphanumericValueExpression("update")),
                Pair(
                    AlphanumericValueExpression("data"), MapValueExpression(
                        mapOf(
                            Pair(AlphanumericValueExpression("name"), AlphanumericValueExpression("new test name")),
                            Pair(
                                AlphanumericValueExpression("where"), MapValueExpression(
                                    mapOf(
                                        Pair(AlphanumericValueExpression("id"), CallChainExpression(listOf(NumberValueExpression("2"), FunctionCallExpression("toInt")))),
                                    )
                                )
                            ),
                        )
                    )
                )
            ),
            emptyMap()
        )
        processor.process(updateTestStep2)
            .subscribe { _, _, e ->
                assertEquals("", e?.message ?: "")
            }

        val validationTestStep = factory.create(
            "t3",
            mapOf(
                Pair(AlphanumericValueExpression("provider"), AlphanumericValueExpression("cassandra")),
                Pair(AlphanumericValueExpression("table"), StringValueExpression(TABLE)),
                Pair(AlphanumericValueExpression("command"), AlphanumericValueExpression("select")),
                Pair(
                    AlphanumericValueExpression("where"), MapValueExpression(
                        mapOf(
                            Pair(AlphanumericValueExpression("id"), CallChainExpression(listOf(NumberValueExpression("2"), FunctionCallExpression("toInt"))))
                        )
                    )
                ),
                Pair(
                    AlphanumericValueExpression("validate"), MapValueExpression(
                        mapOf(
                            Pair(
                                FunctionCallExpression("first"), MapValueExpression(
                                    mapOf(
                                        Pair(AlphanumericValueExpression("name"), AlphanumericValueExpression("new test name")),
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            emptyMap()
        )
        processor.process(validationTestStep)
            .subscribe { _, _, e ->
                assertEquals("", e?.message ?: "")
            }

    }

    private fun initDatabase() {
        processingDetails = cassandraContainers.map { container ->
            val cluster = container.cluster
            cluster.connect().use { session ->
                session.execute(
                    """
    CREATE KEYSPACE IF NOT EXISTS books WITH replication = 
    {'class':'SimpleStrategy','replication_factor':'1'};
    """.trimIndent()
                )
                session.execute(
                    "create table books.book" +
                            "(" +
                            "id int primary key," +
                            "date_created timestamp," +
                            "name text" +
                            ")" +
                            "with caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}"
                )
            }

            val state = DefaultTestScenarioState()
            System.setProperty("container_mapped_port", container.getMappedPort(9042).toString())
            // some dependencies are mocked because they won't be used in the test
            val config = CassandraTestStepProcessorConfig().config(
                TestStepProcessorConfigDetails(
                    state,
                    ConfigFactory.load(this.javaClass.classLoader, "cassandra-integration-test.conf"),
                    mock<TestStepRegistry>(),
                    ValueExpressionContextFactory(
                        DefaultFunctionValueExecutor.Builder()
                            .withTestScenarioState(state)
                            .withClassLoader(CassandraTestProcessingIT::class.java.classLoader)
                            .build(), DefaultPropertyExtractor(null)
                    ),
                    mock<TestStepProcessor<TestStep>>(),
                    mock<TestStepFactory<TestStep>>(),
                )
            )!!

            Pair(config.testStepFactory, config.testStepProcessor)
        }.toList()
    }

    private fun createContainer(version: String) = CassandraContainer("cassandra:$version")
        .withReuse(true)
        .withExposedPorts(9042)

}