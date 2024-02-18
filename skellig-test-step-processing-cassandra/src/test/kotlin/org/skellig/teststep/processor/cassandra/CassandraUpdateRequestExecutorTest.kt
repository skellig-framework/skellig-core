package org.skellig.teststep.processor.cassandra

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.ResultSet
import com.datastax.oss.driver.api.core.cql.SimpleStatement
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.skellig.teststep.processor.db.model.DatabaseRequest
import java.time.LocalDate
import java.time.LocalDateTime

//TODO: fix tests
@Disabled
@DisplayName("Execute update")
internal class CassandraUpdateRequestExecutorTest {

    private var session: CqlSession = mock()
    private var insertRequestExecutor: CassandraInsertRequestExecutor = mock()
    private var selectRequestExecutor: CassandraSelectRequestExecutor = mock()
    private var updateExecutor = CassandraUpdateRequestExecutor(session, insertRequestExecutor, selectRequestExecutor)


    @Test
    @DisplayName("When query without parameters Then verify executed successfully")
    fun testUpdateUsingQuery() {
        val databaseRequest = DatabaseRequest("update query")

        val resultSet = makeSessionReturnResultSet(databaseRequest.query!!, 0)

        assertEquals(resultSet, updateExecutor.execute(databaseRequest))
    }

    @Test
    @DisplayName("When query with parameters Then verify executed successfully with parameters")
    fun testUpdateUsingQueryWithParameters() {
        val databaseRequest = DatabaseRequest("update query", listOf("p1"))

        val resultSet = makeSessionReturnResultSet(databaseRequest.query!!, databaseRequest.queryParameters!!.count())

        assertEquals(resultSet, updateExecutor.execute(databaseRequest))
    }

    @Test
    @DisplayName("When command provided Then verify query created And executed successfully")
    fun testUpdateUsingCommand() {
        val sql = "UPDATE t1 SET c3=?,c2=? WHERE c1=?"
        val data = hashMapOf(
            Pair("where", mapOf(Pair("c1", "v1"))),
            Pair("c2", LocalDateTime.of(2020, 1, 1, 10, 10)),
            Pair("c3", LocalDate.of(2020, 2, 2))
        )

        val databaseRequest = DatabaseRequest("update", "t1", data)
        val resultSet = makeSessionReturnResultSet(sql, data.size)
        whenever(selectRequestExecutor.execute(argThat { o -> o.query == "select" })).thenReturn(listOf("record with id c1 = v1"))

        assertEquals(resultSet, updateExecutor.execute(databaseRequest))
    }

    @Test
    @DisplayName("When record not exist Then verify it is inserted")
    fun testUpdateWhenRecordNotExist() {
        val data = hashMapOf(
            Pair("where", mapOf(Pair("c1", "v1"), Pair("c4", "v4"))),
            Pair("c3", "v3")
        )

        val databaseRequest = DatabaseRequest("update", "t1", data)
        whenever(selectRequestExecutor.execute(any())).thenReturn(emptyList<String>())
        whenever(insertRequestExecutor.execute(argThat { o ->
            o.table == databaseRequest.table &&
                    o.columnValuePairs!!["c1"] == "v1" &&
                    o.columnValuePairs!!["c3"] == "v3" &&
                    o.columnValuePairs!!["c4"] == "v4"
        })).thenReturn(1)

        assertEquals(1, updateExecutor.execute(databaseRequest))
    }

    @Test
    @DisplayName("When Where clause not provided Then throw exception")
    fun testUpdateWhenWhereClauseNotProvided() {
        val databaseRequest = DatabaseRequest("update", "t1", hashMapOf(Pair("c3", "v3")))

        val ex = assertThrows(IllegalStateException::class.java) { updateExecutor.execute(databaseRequest) }

        assertEquals(
            "Update operation for table t1 must have 'where' clause to understand which records to update",
            ex.message
        )
    }

    @Test
    @DisplayName("When no data provided for table Then throw exception")
    fun testUpdateUsingCommandWithoutData() {
        val databaseRequest = DatabaseRequest("update", "t1", null)

        val ex = assertThrows(IllegalStateException::class.java) { updateExecutor.execute(databaseRequest) }

        assertEquals("Cannot update empty data in table t1", ex.message)
    }

    private fun makeSessionReturnResultSet(sql: String, parametersCount: Int): ResultSet {
        val resultSet = mock<ResultSet>()
        whenever(session.execute(any<SimpleStatement>()))
            .thenAnswer { o: InvocationOnMock ->
                val statement = o.arguments[0] as SimpleStatement
                if (statement.query == sql &&
                    statement.positionalValues.size == parametersCount
                ) {
                    return@thenAnswer resultSet
                } else {
                    return@thenAnswer null
                }
            }
        return resultSet
    }
}