package org.skellig.teststep.processor.cassandra

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skellig.teststep.processor.cassandra.model.CassandraDetails
import org.skellig.teststep.processor.db.model.DatabaseRequest
import org.testcontainers.containers.CassandraContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.InetSocketAddress
import java.sql.Date
import java.time.LocalDateTime
import java.time.ZoneOffset

@Testcontainers
internal class CassandraRequestExecutorCT {

    companion object {
        private const val TABLE = "books.book"
        private const val USER_NAME = "cassandra"
        private const val PASSWORD = "cassandra"
    }

    @Container
    private val cassandraContainer = CassandraContainer<CassandraContainer<*>>("cassandra:4.0")
            .withExposedPorts(9042)

    @AfterEach
    fun tearDown() {
        cassandraContainer.close()
    }

    @Test
    @DisplayName("Insert and read data from table Then verify response is correct")
    fun testInsertAndReadFromDb() {
        initDatabase()
        val requestExecutor = CassandraRequestExecutor(createCassandraDetails())
        val insertParams = hashMapOf(
                Pair("id", 1),
                Pair("date_created", LocalDateTime.now()),
                Pair("name", "test"))

        requestExecutor.execute(DatabaseRequest("insert", TABLE, insertParams))
        val selectParams = hashMapOf(Pair("id", 1))

        val response = requestExecutor.execute(DatabaseRequest("select", TABLE, selectParams))
        val row = (response as List<*>?)!![0] as Map<*, *>

        Assertions.assertAll(
                { Assertions.assertEquals(insertParams["id"], row["id"]) },
                { Assertions.assertEquals(Date.from((insertParams["date_created"] as LocalDateTime?)!!.toInstant(ZoneOffset.UTC)), row["date_created"]) },
                { Assertions.assertEquals(insertParams["name"], row["name"]) }
        )
    }

    private fun initDatabase() {
        val cluster = cassandraContainer.cluster
        cluster.connect().use { session ->
            session.execute("""
    CREATE KEYSPACE IF NOT EXISTS books WITH replication = 
    {'class':'SimpleStrategy','replication_factor':'1'};
    """.trimIndent())
            session.execute("create table books.book" +
                    "(" +
                    "id int primary key," +
                    "date_created timestamp," +
                    "name text" +
                    ")" +
                    "with caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}")
        }
    }

    private fun createCassandraDetails(): CassandraDetails {
        return CassandraDetails("s1", listOf(InetSocketAddress("localhost", cassandraContainer.getMappedPort(9042))),
                USER_NAME, PASSWORD)
    }
}