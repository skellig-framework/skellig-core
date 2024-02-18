package org.skellig.teststep.processor.jdbc


import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.*
import org.skellig.teststep.processor.db.model.DatabaseRequest
import java.sql.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

@DisplayName("Execute select request")
internal class JdbcSelectRequestExecutorTest {

    private var executorSelect: JdbcSelectRequestExecutor? = null
    private var connection: Connection? = null

    @BeforeEach
    fun setUp() {
        connection = Mockito.mock(Connection::class.java)
        executorSelect = JdbcSelectRequestExecutor(connection)
    }

    @Test
    @DisplayName("When query provided with no parameters Then check parameters ignored")
    fun testFindUsingQuery() {
        val sql = "select query"
        val databaseRequest = Mockito.mock(DatabaseRequest::class.java)
        whenever(databaseRequest.query).thenReturn(sql)

        val resultSet = createResultSet()
        val statement = Mockito.mock(PreparedStatement::class.java)
        whenever(statement.executeQuery()).thenReturn(resultSet)
        whenever(connection!!.prepareStatement(sql)).thenReturn(statement)

        val response = executorSelect!!.execute(databaseRequest) as List<Map<*, *>>?

        Assertions.assertAll(
                { Assertions.assertEquals(1, response!!.size) },
                { Assertions.assertEquals(3, response!![0].size) },
                { Assertions.assertEquals("v1", response!![0]["c1"]) },
                { Assertions.assertEquals("v2", response!![0]["c2"]) },
                { Assertions.assertNull(response!![0]["c3"]) },
                { verify(statement, times(0)).setObject(any(), any()) }
        )
    }

    @Test
    @DisplayName("When query provided with parameters Then check parameters applied")
    fun testFindUsingQueryWithParameters() {
        val queryParameters = listOf(LocalDateTime.of(2020, 1, 1, 10, 10), 10)
        val databaseRequest = DatabaseRequest("select * from A where c2 = ? OR c1 > ?", queryParameters)
        val resultSet = createResultSet()
        val statement = Mockito.mock(PreparedStatement::class.java)
        whenever(statement.executeQuery()).thenReturn(resultSet)
        whenever(connection!!.prepareStatement(databaseRequest.query)).thenReturn(statement)

        val response = executorSelect!!.execute(databaseRequest) as List<Map<*, *>>?

        Assertions.assertAll(
            { Assertions.assertEquals(1, response!!.size) },
            { verify(statement).setObject(1, Timestamp.valueOf(queryParameters[0] as LocalDateTime)) },
            { verify(statement).setObject(2, queryParameters[1]) }
        )
    }

    @Test
    @DisplayName("When command provided without filter Then check query created correctly")
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
    @DisplayName("When command provided with filter Then check query created correctly")
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

    private fun createResultSet(): ResultSet {
        val resultSet = Mockito.mock(ResultSet::class.java)
        val metaData = Mockito.mock(ResultSetMetaData::class.java)
        whenever(metaData.columnCount).thenReturn(3)
        whenever(metaData.getColumnName(1)).thenReturn("c1")
        whenever(metaData.getColumnName(2)).thenReturn("c2")
        whenever(metaData.getColumnName(3)).thenReturn("c3")
        whenever(resultSet.metaData).thenReturn(metaData)
        whenever(resultSet.getObject("c1")).thenReturn("v1")
        whenever(resultSet.getObject("c2")).thenReturn("v2")
        whenever(resultSet.getObject("c3")).thenReturn(null)

        // has only 1 row
        val counter = AtomicInteger(0)
        whenever(resultSet.next()).thenAnswer { counter.incrementAndGet() <= 1 }

        return resultSet
    }
}