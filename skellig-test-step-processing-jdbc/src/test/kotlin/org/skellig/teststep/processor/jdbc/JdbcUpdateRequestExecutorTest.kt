package org.skellig.teststep.processor.jdbc

import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.skellig.teststep.processor.db.model.DatabaseRequest
import java.sql.*
import java.time.LocalDate
import java.time.LocalDateTime

internal class JdbcUpdateRequestExecutorTest {

    private var updateExecutor: JdbcUpdateRequestExecutor? = null
    private var connection: Connection? = null
    private var insertRequestExecutor: JdbcInsertRequestExecutor? = null
    private var selectRequestExecutor: JdbcSelectRequestExecutor? = null

    @BeforeEach
    fun setUp() {
        connection = mock()
        insertRequestExecutor = mock()
        selectRequestExecutor = mock()
        updateExecutor = JdbcUpdateRequestExecutor(connection, selectRequestExecutor!!, insertRequestExecutor!!)
    }

    @Test
    fun testUpdateUsingQuery() {
        val databaseRequest = Mockito.mock(DatabaseRequest::class.java)
        whenever(databaseRequest.query).thenReturn("update query")

        val statement = mock<Statement>()
        whenever(statement.executeUpdate(databaseRequest.query)).thenReturn(1)
        whenever(connection!!.createStatement()).thenReturn(statement)

        assertEquals(1, updateExecutor!!.execute(databaseRequest))
    }

    @Test
    fun testUpdateUsingCommand() {
        val sql = "UPDATE t1 SET c3=?,c4=?,c2=? WHERE c1=?"
        val data = hashMapOf(
                Pair("where", mapOf(Pair("c1", "v1"))),
                Pair("c2", 2),
                Pair("c3", LocalDateTime.of(2020, 1, 1, 10, 10)),
                Pair("c4", LocalDate.of(2020, 2, 2)))

        val databaseRequest = DatabaseRequest("update", "t1", data)
        val statement = mock<PreparedStatement>()
        whenever(statement.executeUpdate()).thenReturn(1)
        whenever(connection!!.prepareStatement(sql)).thenReturn(statement)
        whenever(selectRequestExecutor!!.execute(any())).thenReturn(listOf("record with id c1 = v1"))

        assertAll(
                { assertEquals(1, updateExecutor!!.execute(databaseRequest)) },
                {
                    verify(statement).setObject(eq(1), argThat { o ->
                        o is Timestamp && o.compareTo(Timestamp.valueOf(data["c3"] as LocalDateTime?)) == 0
                    })
                },
                {
                    verify(statement).setObject(eq(2), argThat { o ->
                        o is Date && o.compareTo(Date.valueOf(data["c4"] as LocalDate?)) == 0
                    })
                },
                { verify(statement).setObject(3, 2) },
                { verify(statement).setObject(4, "v1") }
        )
    }

    @Test
    fun testUpdateWhenRecordNotExist() {
        val data = hashMapOf(
                Pair("where", mapOf(Pair("c1", "v1"), Pair("c4", "v4"))),
                Pair("c3", "v3"))

        val databaseRequest = DatabaseRequest("update", "t1", data)
        whenever(selectRequestExecutor!!.execute(any())).thenReturn(emptyList<String>())
        whenever(insertRequestExecutor!!.execute(argThat { o ->
            o.table == databaseRequest.table &&
                    o.columnValuePairs!!["c1"] == "v1" &&
                    o.columnValuePairs!!["c3"] == "v3" &&
                    o.columnValuePairs!!["c4"] == "v4"
        })).thenReturn(1)

        assertEquals(1, updateExecutor!!.execute(databaseRequest))
    }

    @Test
    fun testUpdateWhenWhereClauseNotProvided() {
        val databaseRequest = DatabaseRequest("update", "t1", hashMapOf(Pair("c3", "v3")))

        val ex = assertThrows(IllegalStateException::class.java) { updateExecutor!!.execute(databaseRequest) }

        assertEquals("Update operation for table t1 must have 'where' clause to understand which records to update", ex.message)
    }

    @Test
    fun testUpdateUsingCommandWithoutData() {
        val databaseRequest = DatabaseRequest("update", "t1", null)

        val ex = assertThrows(IllegalStateException::class.java) { updateExecutor!!.execute(databaseRequest) }

        assertEquals("Cannot update empty data in table t1", ex.message)
    }
}