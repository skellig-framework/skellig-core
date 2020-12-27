package org.skellig.teststep.processor.jdbc

import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.mockito.Mockito
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.db.model.DatabaseRequest
import java.sql.*
import java.time.LocalDate
import java.time.LocalDateTime

internal class InsertJdbcRequestExecutorTest {

    private var executor: InsertJdbcRequestExecutor? = null
    private var connection: Connection? = null

    @BeforeEach
    fun setUp() {
        connection = Mockito.mock(Connection::class.java)
        executor = InsertJdbcRequestExecutor(connection)
    }

    @Test
    @Throws(SQLException::class)
    fun testInsertUsingQuery() {
        val databaseRequest = Mockito.mock(DatabaseRequest::class.java)
        whenever(databaseRequest.query).thenReturn("insert query")

        val statement = Mockito.mock(Statement::class.java)
        whenever(statement.executeUpdate(databaseRequest.query)).thenReturn(1)
        whenever(connection!!.createStatement()).thenReturn(statement)

        Assertions.assertEquals(1, executor!!.execute(databaseRequest))
    }

    @Test
    @Throws(SQLException::class)
    fun testInsertUsingCommand() {
        val sql = "insert INTO t1 (c3,c4,c1,c2) VALUES(?,?,?,?)"
        val data = hashMapOf(
                Pair("c1", "v1"),
                Pair("c2", 2),
                Pair("c3", LocalDateTime.of(2020, 1, 1, 10, 10)),
                Pair("c4", LocalDate.of(2020, 2, 2)))

        val databaseRequest = DatabaseRequest("insert", "t1", data)
        val statement = Mockito.mock(PreparedStatement::class.java)
        whenever(statement.executeUpdate()).thenReturn(1)
        whenever(connection!!.prepareStatement(sql)).thenReturn(statement)

        Assertions.assertAll(
                Executable { Assertions.assertEquals(1, executor!!.execute(databaseRequest)) },
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

        val ex = Assertions.assertThrows(TestStepProcessingException::class.java) { executor!!.execute(databaseRequest) }

        Assertions.assertEquals("Cannot insert empty data to table t1", ex.message)
    }
}