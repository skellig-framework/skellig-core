package org.skellig.teststep.processor.db

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.db.model.DatabaseRequest

class DatabaseRequestExecutorFactoryTest {

    private var factory: DatabaseRequestExecutorFactory? = null
    private var selectExecutor = Mockito.mock(DatabaseRequestExecutor::class.java)
    private var insertExecutor = Mockito.mock(DatabaseRequestExecutor::class.java)

    @BeforeEach
    fun setUp() {
        factory = DatabaseRequestExecutorFactory(selectExecutor, insertExecutor)
    }

    @Test
    fun testGetRequestExecutorForSelectCommand() {
        val databaseRequest = DatabaseRequest(command = "select")

        val result = factory!![databaseRequest]

        Assertions.assertEquals(selectExecutor, result)
    }

    @Test
    fun testGetRequestExecutorForInsertCommand() {
        val databaseRequest = DatabaseRequest(command = "insert")

        val result = factory!![databaseRequest]

        Assertions.assertEquals(insertExecutor, result)
    }

    @Test
    fun testGetRequestExecutorForInsertQuery() {
        val databaseRequest = DatabaseRequest(query = " insert into V(select, gg, bb)")

        val result = factory!![databaseRequest]

        Assertions.assertEquals(insertExecutor, result)
    }

    @Test
    fun testGetRequestExecutorForSelectQuery() {
        val databaseRequest = DatabaseRequest(query = " select * from V when a = insert")

        val result = factory!![databaseRequest]

        Assertions.assertEquals(selectExecutor, result)
    }

    @Test
    fun testGetRequestExecutorForUndefinedQuery() {
        val databaseRequest = DatabaseRequest(query = "some query")

        val ex = Assertions.assertThrows(TestStepProcessingException::class.java) { factory!![databaseRequest] }

        Assertions.assertEquals("No database query executors found for query: 'some query'." +
                " Supported types of queries: [select, insert]", ex.message)
    }

    @Test
    fun testGetRequestExecutorForUndefinedCommand() {
        val databaseRequest = DatabaseRequest(command = "wrong command")

        val ex = Assertions.assertThrows(TestStepProcessingException::class.java) { factory!![databaseRequest] }

        Assertions.assertEquals("No database query executors found for command: 'wrong command'." +
                " Supported commands: [select, insert]", ex.message)
    }
}