package org.skellig.teststep.processor.jdbc

import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.skellig.teststep.processor.db.model.DatabaseRequest
import java.sql.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

internal class JdbcSelectRequestExecutorTest {

    private var executorSelect: JdbcSelectRequestExecutor? = null
    private var connection: Connection? = null

    @BeforeEach
    fun setUp() {
        connection = Mockito.mock(Connection::class.java)
        executorSelect = JdbcSelectRequestExecutor(connection)
    }

    @Test
    @Throws(SQLException::class)
    fun testFindUsingQuery() {
        val databaseRequest = Mockito.mock(DatabaseRequest::class.java)
        whenever(databaseRequest.query).thenReturn("select query")

        val resultSet = createResultSet()
        val statement = Mockito.mock(Statement::class.java)
        whenever(statement.executeQuery(databaseRequest.query)).thenReturn(resultSet)
        whenever(connection!!.createStatement()).thenReturn(statement)

        val response = executorSelect!!.execute(databaseRequest) as List<Map<*, *>>?

        Assertions.assertAll(
                { Assertions.assertEquals(1, response!!.size) },
                { Assertions.assertEquals(2, response!![0].size) },
                { Assertions.assertEquals("v1", response!![0]["c1"]) },
                { Assertions.assertEquals("v2", response!![0]["c2"]) }
        )
    }

    @Test
    @Throws(SQLException::class)
    fun testFindUsingCommandWithoutFilter() {
        val sql = "SELECT * FROM t1"
        val databaseRequest = DatabaseRequest("select", "t1", null)

        val resultSet = createResultSet()
        val statement = Mockito.mock(PreparedStatement::class.java)
        whenever(statement.executeQuery()).thenReturn(resultSet)
        whenever(connection!!.prepareStatement(sql)).thenReturn(statement)

        val response = executorSelect!!.execute(databaseRequest) as List<Map<*, *>>?

        Assertions.assertEquals(1, response!!.size)
    }

    @Test
    @Throws(SQLException::class)
    fun testFindUsingCommandWithFilter() {
        val sql = "SELECT * FROM t1 WHERE c3 like ? AND c4 in (?) AND c5 = ? AND c6 = ? AND c1 = ? AND c2 > ?"
        val filter = hashMapOf(
                Pair("c1", "v1"),
                Pair("c2", mapOf(
                        Pair("comparator", ">"),
                        Pair("value", 10),
                )),
                Pair("c3", mapOf(
                        Pair("comparator", "like"),
                        Pair("value", "%a%"),
                )),
                Pair("c4", mapOf(
                        Pair("comparator", "in"),
                        Pair("value", listOf("a", "b")),
                )),
                Pair("c5", LocalDateTime.of(2020, 1, 1, 10, 10)),
                Pair("c6", LocalDate.of(2020, 2, 2)),
        )


        val databaseRequest = DatabaseRequest("select", "t1", filter)

        val resultSet = createResultSet()
        val statement = Mockito.mock(PreparedStatement::class.java)
        whenever(statement.executeQuery()).thenReturn(resultSet)
        whenever(connection!!.prepareStatement(sql)).thenReturn(statement)

        val response = executorSelect!!.execute(databaseRequest) as List<Map<*, *>>

        Assertions.assertAll(
                { Assertions.assertEquals(1, response.size) },
                { Mockito.verify(statement).setObject(1, "%a%") },
                {
                    Mockito.verify(statement).setObject(eq(2), argThat { o -> (o as List<*>).contains("a") && o.contains("b") })
                },
                {
                    Mockito.verify(statement).setObject(eq(3), argThat { o ->
                        o is Timestamp && o.compareTo(Timestamp.valueOf(filter["c5"] as LocalDateTime?)) == 0
                    })
                },
                {
                    Mockito.verify(statement).setObject(eq(4), argThat { o ->
                        o is Date && o.compareTo(Date.valueOf(filter["c6"] as LocalDate?)) == 0
                    })
                },
                { Mockito.verify(statement).setObject(5, "v1") },
                { Mockito.verify(statement).setObject(6, 10) }
        )
    }

    @Throws(SQLException::class)
    private fun createResultSet(): ResultSet {
        val resultSet = Mockito.mock(ResultSet::class.java)
        val metaData = Mockito.mock(ResultSetMetaData::class.java)
        whenever(metaData.columnCount).thenReturn(2)
        whenever(metaData.getColumnName(1)).thenReturn("c1")
        whenever(metaData.getColumnName(2)).thenReturn("c2")
        whenever(resultSet.metaData).thenReturn(metaData)
        whenever(resultSet.getObject("c1")).thenReturn("v1")
        whenever(resultSet.getObject("c2")).thenReturn("v2")

        // has only 1 row
        val counter = AtomicInteger(0)
        whenever(resultSet.next()).thenAnswer { counter.incrementAndGet() <= 1 }

        return resultSet
    }
}