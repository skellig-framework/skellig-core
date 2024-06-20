package org.skellig.teststep.processor.db

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processor.db.model.DatabaseTestStep

@DisplayName("Process database test step")
class DatabaseTestStepProcessorTest {

    companion object {
        private const val SRV_1 = "srv1"
        private const val SRV_2 = "srv2"
    }

    private var databaseTestStepProcessor: DatabaseTestStepProcessor<*, TestDatabaseTestStep>? = null
    private var dbRequestExecutor1 = mock<DatabaseRequestExecutor>()
    private var dbRequestExecutor2 = mock<DatabaseRequestExecutor>()

    @BeforeEach
    fun setUp() {
        val dbServers = mapOf(
            Pair("srv1", dbRequestExecutor1),
            Pair("srv2", dbRequestExecutor2)
        )

        databaseTestStepProcessor = object : DatabaseTestStepProcessor<DatabaseRequestExecutor, TestDatabaseTestStep>(
            dbServers, mock<TestScenarioState>()
        ) {
            override fun getTestStepClass(): Class<*> = TestDatabaseTestStep::class.java
        }
    }

    @Test
    @DisplayName("When no servers are provided Then throw exception")
    fun testProcessDatabaseTestStepWhenNoServersProvided() {
        val testStep = TestDatabaseTestStepBuilder().withName("n1").build()

        databaseTestStepProcessor!!.process(testStep)
            .subscribe { _, _, e ->
                assertEquals(
                    "No DB servers were provided to run a query." +
                            " Registered servers are: [srv1, srv2]", e!!.message
                )
            }
    }

    @Test
    @DisplayName("When no server is registered Then throw exception")
    fun testProcessDatabaseTestStepWhenNoServerIsRegistered() {
        val responseFromDb = Any()
        whenever(dbRequestExecutor1.execute(argThat { false })).thenReturn(responseFromDb)
        val testStep = TestDatabaseTestStepBuilder()
            .withServers(listOf("default"))
            .withName("n1")
            .build()

        databaseTestStepProcessor!!.process(testStep)
            .subscribe { _, _, e ->
                assertEquals(
                    "No database was registered for server name 'default'." +
                            " Registered servers are: [srv1, srv2]", e!!.message
                )
            }
    }

    @Test
    @DisplayName("When run only on one db server And only one server is registered Then verify grouped response returned")
    fun testProcessDatabaseTestStepForSingleServer() {
        val testStep = TestDatabaseTestStepBuilder()
            .withServers(listOf(SRV_1))
            .withCommand("select")
            .withTable("t1")
            .withName("n1")
            .build()
        val responseFromDb = Any()
        whenever(dbRequestExecutor1.execute(argThat { request ->
            request.command == testStep.command && request.table == testStep.table
        })).thenReturn(responseFromDb)

        databaseTestStepProcessor!!.process(testStep)
            .subscribe { _, r, _ -> assertEquals(responseFromDb, (r as Map<*, *>?)!![SRV_1]) }
    }

    @Test
    @DisplayName("When run on 2 db servers And 2 servers provided in step and only query provided Then verify grouped response returned")
    fun testProcessDatabaseTestStepForTwoServersWhenQueryProvided() {
        val testStep = TestDatabaseTestStepBuilder()
            .withServers(listOf(SRV_1, SRV_2))
            .withQuery("select * from t1")
            .withName("n1")
            .build()

        // return results from all 2 db servers
        val responseFromDb1 = Any()
        whenever(dbRequestExecutor1.execute(argThat { request -> request.query == testStep.query }))
            .thenReturn(responseFromDb1)
        val responseFromDb2 = Any()
        whenever(dbRequestExecutor2.execute(argThat { request -> request.query == testStep.query }))
            .thenReturn(responseFromDb2)

        databaseTestStepProcessor!!.process(testStep)
            .subscribe { _, r, _ ->
                assertEquals(responseFromDb1, (r as Map<*, *>?)!![SRV_1])
                assertEquals(responseFromDb2, r!![SRV_2])
            }
    }

    @Test
    @DisplayName("When run only on one db server And server not provided in step Then response is not grouped by servers")
    fun testProcessDatabaseTestStepWhenNoServersProvidedAndOneRegistered() {
        databaseTestStepProcessor = object : DatabaseTestStepProcessor<DatabaseRequestExecutor, TestDatabaseTestStep>(
            mapOf(Pair("srv2", dbRequestExecutor1)), mock<TestScenarioState>()
        ) {
            override fun getTestStepClass(): Class<*> = TestDatabaseTestStep::class.java
        }

        val testStep = TestDatabaseTestStepBuilder()
            .withCommand("select")
            .withTable("t1")
            .withName("n1")
            .build()
        val responseFromDb = Any()
        whenever(dbRequestExecutor1.execute(argThat { request ->
            request.command == testStep.command && request.table == testStep.table
        })).thenReturn(responseFromDb)

        databaseTestStepProcessor!!.process(testStep)
            .subscribe { _, r, _ -> assertEquals(responseFromDb, r) }
    }

    @Test
    fun `verify toString of Test Step`() {
        assertEquals(
            "name = n1\n" +
                    "servers = [srv1, srv2]\n" +
                    "query=select * from t1\n",
            TestDatabaseTestStepBuilder()
                .withServers(listOf(SRV_1, SRV_2))
                .withQuery("select * from t1")
                .withName("n1")
                .build()
                .toString()
        )
        assertEquals(
            "name = n1\n" +
                    "servers = [srv1]\n" +
                    "command = select\n" +
                    "table = t1\n",
            TestDatabaseTestStepBuilder()
                .withServers(listOf(SRV_1))
                .withCommand("select")
                .withTable("t1")
                .withName("n1")
                .build()
                .toString()
        )
    }

    private inner class TestDatabaseTestStepBuilder : DatabaseTestStep.Builder<TestDatabaseTestStep>() {
        override fun build(): TestDatabaseTestStep = TestDatabaseTestStep(name!!, servers, command, table, query)
    }

    private inner class TestDatabaseTestStep(
        name: String,
        servers: Collection<String>?,
        command: String?,
        table: String?,
        query: String?
    ) : DatabaseTestStep(
        null, name, TestStepExecutionType.SYNC, 0, 0, 0, null, null, null, null,
        servers, command, table, query
    )
}