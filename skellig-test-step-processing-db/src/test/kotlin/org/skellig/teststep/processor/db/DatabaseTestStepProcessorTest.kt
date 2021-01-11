package org.skellig.teststep.processor.db

import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.DefaultTestStepResultValidator
import org.skellig.teststep.processing.validation.comparator.ValueComparator
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor
import org.skellig.teststep.processor.db.model.DatabaseTestStep

@DisplayName("Process database test step")
class DatabaseTestStepProcessorTest {

    companion object {
        private const val SRV_1 = "srv1"
        private const val SRV_2 = "srv2"
    }

    private var databaseTestStepProcessor: DatabaseTestStepProcessor<*>? = null
    private var dbRequestExecutor1 = mock(DatabaseRequestExecutor::class.java)
    private var dbRequestExecutor2 = mock(DatabaseRequestExecutor::class.java)
    private var testStepResultConverter: TestStepResultConverter? = null

    @BeforeEach
    fun setUp() {
        testStepResultConverter = mock(TestStepResultConverter::class.java)

        val dbServers = mapOf(
                Pair("srv1", dbRequestExecutor1),
                Pair("srv2", dbRequestExecutor2))

        databaseTestStepProcessor = object : DatabaseTestStepProcessor<DatabaseRequestExecutor>(
                dbServers, mock(TestScenarioState::class.java),
                DefaultTestStepResultValidator.Builder()
                        .withValueComparator(mock(ValueComparator::class.java))
                        .withValueExtractor(mock(TestStepValueExtractor::class.java))
                        .build(),
                testStepResultConverter
        ) {}
    }

    @Test
    @DisplayName("When no servers are provided Then throw exception")
    fun testProcessDatabaseTestStepWhenNoServersProvided() {
        val testStep = DatabaseTestStep.Builder().withName("n1").build()

        databaseTestStepProcessor!!.process(testStep as DatabaseTestStep)
                .subscribe { _, _, e ->
                    Assertions.assertEquals("No DB servers were provided to run a query." +
                            " Registered servers are: [srv1, srv2]", e!!.message)
                }
    }

    @Test
    @DisplayName("When no server is registered Then throw exception")
    fun testProcessDatabaseTestStepWhenNoServerIsRegistered() {
        val responseFromDb = Any()
        whenever(dbRequestExecutor1!!.execute(argThat { false })).thenReturn(responseFromDb)
        val testStep = DatabaseTestStep.Builder()
                .withServers(listOf("default"))
                .withName("n1")
                .build()

        databaseTestStepProcessor!!.process(testStep as DatabaseTestStep)
                .subscribe { _, _, e ->
                    Assertions.assertEquals("No database was registered for server name 'default'." +
                            " Registered servers are: [srv1, srv2]", e!!.message)
                }
    }

    @Test
    @DisplayName("When run only on one db server Then verify single response returned")
    fun testProcessDatabaseTestStepForSingleServer() {
        val testStep = DatabaseTestStep.Builder()
                .withServers(listOf(SRV_1))
                .withCommand("select")
                .withTable("t1")
                .withName("n1")
                .build() as DatabaseTestStep
        val responseFromDb = Any()
        whenever(dbRequestExecutor1!!.execute(argThat { request ->
            request.command == testStep.command && request.table == testStep.table
        }))
                .thenReturn(responseFromDb)

        databaseTestStepProcessor!!.process(testStep)
                .subscribe { _, r, _ -> Assertions.assertEquals(responseFromDb, (r as Map<*, *>?)!![SRV_1]) }
    }

    @Test
    @DisplayName("When run on 2 db servers And only query provided Then verify grouped response returned")
    fun testProcessDatabaseTestStepForTwoServersWhenQueryProvided() {
        val testStep = DatabaseTestStep.Builder()
                .withServers(listOf(SRV_1, SRV_2))
                .withQuery("select * from t1")
                .withName("n1")
                .build() as DatabaseTestStep

        // return results from all 2 db servers
        val responseFromDb1 = Any()
        whenever(dbRequestExecutor1!!.execute(argThat { request -> request.query == testStep.query }))
                .thenReturn(responseFromDb1)
        val responseFromDb2 = Any()
        whenever(dbRequestExecutor2!!.execute(argThat { request -> request.query == testStep.query }))
                .thenReturn(responseFromDb2)

        databaseTestStepProcessor!!.process(testStep)
                .subscribe { _, r, _ ->
                    Assertions.assertEquals(responseFromDb1, (r as Map<*, *>?)!![SRV_1])
                    Assertions.assertEquals(responseFromDb2, r!![SRV_2])
                }
    }
}