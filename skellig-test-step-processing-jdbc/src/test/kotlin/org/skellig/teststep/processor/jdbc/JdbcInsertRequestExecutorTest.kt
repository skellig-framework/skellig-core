package org.skellig.teststep.processor.jdbc

import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.mockito.Mockito
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.db.model.DatabaseRequest
import java.sql.*
import java.time.LocalDate
import java.time.LocalDateTime

@DisplayName("Execute insert")
internal class JdbcInsertRequestExecutorTest {

    private var executorInsert: JdbcInsertRequestExecutor? = null
    private var connection: Connection? = null

    @BeforeEach
    fun setUp() {
        connection = Mockito.mock(Connection::class.java)
        executorInsert = JdbcInsertRequestExecutor(connection!!)
    }

    @Test
    @DisplayName("When query provided without parameters Then execute successfully")
    fun testInsertUsingQuery() {
        val databaseRequest = Mockito.mock(DatabaseRequest::class.java)
        whenever(databaseRequest.query).thenReturn("insert query")

        val statement = createMockStatement(databaseRequest.query!!)

        Assertions.assertAll(
            { Assertions.assertEquals(1, executorInsert!!.execute(databaseRequest)) },
            { verify(statement, times(0)).setObject(any(), any()) }
        )
    }

    @Test
    @DisplayName("When query provided with parameters Then execute successfully with parameters")
    fun testInsertUsingQueryWithParameters() {
        val databaseRequest = DatabaseRequest("insert query", listOf("1", "2"))

        val statement = createMockStatement(databaseRequest.query!!)

        Assertions.assertAll(
            { Assertions.assertEquals(1, executorInsert!!.execute(databaseRequest)) },
            { verify(statement).setObject(1, "1") },
            { verify(statement).setObject(2, "2") }
        )
    }

    @Test
    @DisplayName("When query provided without parameters Then execute successfully")
    fun testInsertUsingCommand() {
        val sql = "insert INTO t1 (c3,c4,c1,c2) VALUES(?,?,?,?)"
        val data = hashMapOf(
                Pair("c1", "v1"),
                Pair("c2", 2),
                Pair("c3", LocalDateTime.of(2020, 1, 1, 10, 10)),
                Pair("c4", LocalDate.of(2020, 2, 2)))

        val databaseRequest = DatabaseRequest("insert", "t1", data)

        val statement = createMockStatement(sql)

        Assertions.assertAll(
                Executable { Assertions.assertEquals(1, executorInsert!!.execute(databaseRequest)) },
                Executable {
                    Mockito.verify(statement).setObject(eq(1), argThat { o ->
                        o is Timestamp &&
                                o.compareTo(Timestamp.valueOf(data["c3"] as LocalDateTime?)) == 0
                    })
                },
                Executable {
                    Mockito.verify(statement).setObject(eq(2), argThat { o ->
                        o is Date && o.compareTo(Date.valueOf(data["c4"] as LocalDate?)) == 0
                    })
                },
                Executable { Mockito.verify(statement).setObject(3, "v1") },
                Executable { Mockito.verify(statement).setObject(4, 2) }
        )
    }

    @Test
    fun testInsertUsingCommandWithoutData() {
        val databaseRequest = DatabaseRequest("insert", "t1", null)

        val ex = Assertions.assertThrows(TestStepProcessingException::class.java) { executorInsert!!.execute(databaseRequest) }

        Assertions.assertEquals("Cannot insert empty data to table t1", ex.message)
    }

    private fun createMockStatement(sql: String): PreparedStatement {
        val statement = Mockito.mock(PreparedStatement::class.java)
        whenever(statement.executeUpdate()).thenReturn(1)
        whenever(connection!!.prepareStatement(sql)).thenReturn(statement)
        return statement
    }
}