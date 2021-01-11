package org.skellig.teststep.processor.cassandra

import com.datastax.driver.core.*
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.db.model.DatabaseRequest
import java.time.LocalDate
import java.time.LocalDateTime

internal class CassandraInsertRequestExecutorTest {

    private var session = Mockito.mock(Session::class.java)
    private var executor = CassandraInsertRequestExecutor(session)

    @Test
    fun testInsertUsingQuery() {
        val databaseRequest = DatabaseRequest(query = "insert query")

        val response = Mockito.mock(ResultSet::class.java)
        whenever(session.execute(databaseRequest.query)).thenReturn(response)

        Assertions.assertEquals(response, executor.execute(databaseRequest))
    }

    @Test
    fun testInsertUsingCommand() {
        val sql = "insert INTO t1 (c3,c4,c1,c2) VALUES(?,?,?,?)"
        val data = hashMapOf(
                Pair("c1", "v1"),
                Pair("c2", 2),
                Pair("c3", LocalDateTime.of(2020, 1, 1, 10, 10)),
                Pair("c4", LocalDate.of(2020, 2, 2)))
        val databaseRequest = DatabaseRequest("insert", "t1", data)
        val response = Mockito.mock(ResultSet::class.java)
        whenever(session.execute(ArgumentMatchers.any(Statement::class.java))).thenReturn(response)

        val actualResponse = executor.execute(databaseRequest)

        Assertions.assertAll(
                 { Assertions.assertEquals(response, actualResponse) },
                 {
                    Mockito.verify(session).execute(argThat{ o : SimpleStatement ->
                        o.queryString == sql && o.getValues(ProtocolVersion.V3, CodecRegistry.DEFAULT_INSTANCE).size == 4
                    })
                })
    }

    @Test
    fun testInsertUsingCommandWithoutData() {
        val databaseRequest = DatabaseRequest("insert", "t1", null)
        val ex = Assertions.assertThrows(TestStepProcessingException::class.java) { executor.execute(databaseRequest) }

        Assertions.assertEquals("Cannot insert empty data to table t1", ex.message)
    }
}