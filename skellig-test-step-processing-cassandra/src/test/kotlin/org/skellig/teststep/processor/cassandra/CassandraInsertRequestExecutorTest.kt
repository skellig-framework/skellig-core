package org.skellig.teststep.processor.cassandra

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.ResultSet
import com.datastax.oss.driver.api.core.cql.SimpleStatement
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.db.model.DatabaseRequest
import java.time.LocalDate
import java.time.LocalDateTime

internal class CassandraInsertRequestExecutorTest {

    private var session : CqlSession = mock()
    private var executor = CassandraInsertRequestExecutor(session)

    @Test
    fun testInsertUsingQuery() {
        val databaseRequest = DatabaseRequest(query = "insert query")

        val response : ResultSet= mock()
        whenever(session.execute(ArgumentMatchers.any(SimpleStatement::class.java))).thenReturn(response)

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
        val response : ResultSet = mock()
        whenever(session.execute(ArgumentMatchers.any(SimpleStatement::class.java))).thenReturn(response)

        val actualResponse = executor.execute(databaseRequest)

        Assertions.assertAll(
                 { Assertions.assertEquals(response, actualResponse) },
                 {
                    verify(session).execute(argThat<SimpleStatement>{ o : SimpleStatement ->
                        o.query == sql && o.positionalValues.size == 4
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