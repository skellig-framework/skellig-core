package org.skellig.teststep.processor.cassandra

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.ResultSet
import com.datastax.oss.driver.api.core.cql.Row
import com.datastax.oss.driver.api.core.cql.SimpleStatement
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.invocation.InvocationOnMock
import org.skellig.teststep.processor.db.model.DatabaseRequest
import java.util.function.Consumer

internal class CassandraSelectRequestExecutorTest {

    private var session: CqlSession = mock()
    private var executor = CassandraSelectRequestExecutor(session)

    @Test
    fun testFindUsingQuery() {
        val databaseRequest = DatabaseRequest(query = "select query", listOf(1, "2"))

        val resultSet = createResultSet()
        makeSessionReturnResultSet(databaseRequest.query!!, databaseRequest.queryParameters?.size ?: 0, resultSet)

        val response = executor.execute(databaseRequest) as List<Map<*, *>>

        Assertions.assertEquals(1, response.size)
    }

    @Test
    fun testFindUsingCommandWithoutFilter() {
        val sql = "SELECT * FROM t1 ALLOW FILTERING"
        val resultSet = createResultSet()
        makeSessionReturnResultSet(sql, 0, resultSet)
        val response = executor.execute(DatabaseRequest("select", "t1", null)) as List<Map<*, *>>

        Assertions.assertEquals(1, response.size)
    }

    @Test
    fun testFindUsingCommandWithFilter() {
        val sql = "SELECT * FROM t1 WHERE c1 = ? AND c2 = ? ALLOW FILTERING"
        val filter = hashMapOf(
            Pair("c1", "v1"),
            Pair("c2", 20)
        )

        val resultSet = createResultSet()
        makeSessionReturnResultSet(sql, filter.size, resultSet)
        val response = executor.execute(DatabaseRequest("select", "t1", filter)) as List<Map<*, *>>

        Assertions.assertEquals(1, response.size)
    }

    private fun makeSessionReturnResultSet(sql: String, parametersCount: Int, resultSet: ResultSet) {
        whenever(session.execute(ArgumentMatchers.any(SimpleStatement::class.java)))
            .thenAnswer { o: InvocationOnMock ->
                val statement = o.arguments[0] as SimpleStatement
                if (statement.query == sql &&
                    statement.namedValues.size == parametersCount
                ) {
                    return@thenAnswer resultSet
                } else {
                    return@thenAnswer null
                }
            }
    }

    private fun createResultSet(): ResultSet {
        val row: Row = mock()
        whenever(row.getObject("c1")).thenReturn("v1")
        whenever(row.getObject("c2")).thenReturn(20)
        whenever(row.columnDefinitions).thenReturn(mock())
        val resultSet: ResultSet = mock()
        doAnswer { o: InvocationOnMock ->
            (o.arguments[0] as Consumer<in Row>).accept(row)
            o
        }.whenever(resultSet).forEach(ArgumentMatchers.any<Consumer<in Row>>())

        return resultSet
    }
}